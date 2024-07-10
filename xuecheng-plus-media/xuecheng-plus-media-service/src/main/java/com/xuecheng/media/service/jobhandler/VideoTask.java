package com.xuecheng.media.service.jobhandler;

import com.xuecheng.base.utils.Mp4VideoUtil;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileProcessService;
import com.xuecheng.media.service.MediaFileService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.*;

/**
 * 任務處理類
 */
@Slf4j
@Component
public class VideoTask {

    @Autowired
    MediaFileProcessService mediaFileProcessService;

    @Autowired
    MediaFileService mediaFileService;

    //ffmpeg的路徑
    @Value("${videoprocess.ffmpegpath}")
    private String ffmpegpath;

    /**
     * 視頻處理任務
     */
    @XxlJob("videoJobHandler")
    public void videoJobHandler() throws Exception {

        // 分片參數
        int shardIndex = XxlJobHelper.getShardIndex();//執行器的序號，從0開始
        int shardTotal = XxlJobHelper.getShardTotal();//執行器總數

        //確定cpu的核心數
        int processors = Runtime.getRuntime().availableProcessors();
        //查詢待處理的任務
        List<MediaProcess> mediaProcessList = mediaFileProcessService.getMediaProcessList(shardIndex, shardTotal, processors);

        //任務數量
        int size = mediaProcessList.size();
        log.debug("取到視頻處理任務數:"+size);
        if(size<=0){
            return;
        }
        //創建一個線程池
        ExecutorService executorService = Executors.newFixedThreadPool(size);
        //使用的計數器
        CountDownLatch countDownLatch = new CountDownLatch(size);
        mediaProcessList.forEach(mediaProcess -> {
            //將任務加入線程池
            executorService.execute(()->{
                try {
                    //任務id
                    Long taskId = mediaProcess.getId();
                    //文件id就是md5
                    String fileId = mediaProcess.getFileId();
                    //開啟任務
                    boolean b = mediaFileProcessService.startTask(taskId);
                    if (!b) {
                        log.debug("搶占任務失敗,任務id:{}", taskId);
                        return;
                    }

                    //桶
                    String bucket = mediaProcess.getBucket();
                    //objectName
                    String objectName = mediaProcess.getFilePath();

                    //下載minio視頻到本地
                    File file = mediaFileService.downloadFileFromMinIO(bucket, objectName);
                    if (file == null) {
                        log.debug("下載視頻出錯,任務id:{},bucket:{},objectName:{}", taskId, bucket, objectName);
                        //保存任務處理失敗的結果
                        mediaFileProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "下載視頻到本地失敗");
                        return;
                    }

                    //源avi視頻的路徑
                    String video_path = file.getAbsolutePath();
                    //轉換後mp4文件的名稱
                    String mp4_name = fileId + ".mp4";
                    //轉換後mp4文件的路徑
                    //先創建一個臨時文件，作為轉換後的文件
                    File mp4File = null;
                    try {
                        mp4File = File.createTempFile("minio", ".mp4");
                    } catch (IOException e) {
                        log.debug("創建臨時文件異常,{}", e.getMessage());
                        //保存任務處理失敗的結果
                        mediaFileProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "創建臨時文件異常");
                        return;
                    }
                    String mp4_path = mp4File.getAbsolutePath();
                    //創建工具類對象
                    Mp4VideoUtil videoUtil = new Mp4VideoUtil(ffmpegpath, video_path, mp4_name, mp4_path);
                    //開始視頻轉換，成功將返回success,失敗返回失敗原因
                    String result = videoUtil.generateMp4();
                    if (!result.equals("success")) {

                        log.debug("視頻轉碼失敗,原因:{},bucket:{},objectName:{},", result, bucket, objectName);
                        mediaFileProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, result);
                        return;

                    }
                    // String objectName = defaultFolderPath + fileMd5 + extension;
                    //上傳到minio
                    //mp4在minio的存儲路徑
                    objectName = getFilePath(fileId, ".mp4");
                    //訪問url
                    String url = "/" + bucket + "/" + objectName;
                    boolean b1 = mediaFileService.addMediaFilesToMinIO(mp4File.getAbsolutePath(), "video/mp4", bucket, objectName);
                    if (!b1) {
                        log.debug("上傳mp4到minio失敗,taskid:{}", taskId);
                        mediaFileProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "上傳mp4到minio失敗");
                        return;
                    }

                    //更新任務狀態為成功
                    mediaFileProcessService.saveProcessFinishStatus(taskId, "2", fileId, url, "創建臨時文件異常");
                }finally {
                    //計算器減去1
                    countDownLatch.countDown();
                }

            });

        });

        //阻塞,指定最大限制的等待時間，阻塞最多等待一定的時間後就解除阻塞
        countDownLatch.await(30, TimeUnit.MINUTES);


    }

    private String getFilePath(String fileMd5,String fileExt){
        return   fileMd5.substring(0,1) + "/" + fileMd5.substring(1,2) + "/" + fileMd5 + "/" +fileMd5 +fileExt;
    }
}
