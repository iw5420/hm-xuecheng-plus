package com.xuecheng.media.service;

import com.xuecheng.media.model.po.MediaProcess;

import java.util.List;

/**
 * @author Ian
 * @version 1.0
 * @description 媒資文件處理業務方法
 * @date 2024/7/6
 */
public interface MediaFileProcessService {

    /**
     * @description 獲取待處理任務
     * @param shardIndex 分片序號
     * @param shardTotal 分片總數
     * @param count 獲取記錄數
     * @return java.util.List<com.xuecheng.media.model.po.MediaProcess>
     * @author Ian
     * @date 2024/7/6
     */
    public List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal, int count);

    /**
     *  開啟一個任務
     * @param id 任務id
     * @return true開啟任務成功，false開啟任務失敗
     */
    public boolean startTask(long id);

    /**
     * @description 保存任務結果
     * @param taskId  任務id
     * @param status 任務狀態
     * @param fileId  文件id
     * @param url url
     * @param errorMsg 錯誤信息
     * @return void
     * @author Mr.M
     * @date 2022/10/15 11:29
     */
    void saveProcessFinishStatus(Long taskId,String status,String fileId,String url,String errorMsg);

}
