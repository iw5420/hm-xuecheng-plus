package com.xuecheng.content.service.jobhandler;

import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.feignclient.SearchServiceClient;
import com.xuecheng.content.mapper.CoursePublishMapper;
import com.xuecheng.content.model.dto.CourseIndex;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MessageProcessAbstract;
import com.xuecheng.messagesdk.service.MqMessageService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * @author Ian
 * @version 1.0
 * @description TODO
 * @date 2024/7/13
 */
@Slf4j
@Component
public class CoursePublishTask extends MessageProcessAbstract {

    @Autowired
    CoursePublishService coursePublishService;

    @Autowired
    SearchServiceClient searchServiceClient;

    @Autowired
    CoursePublishMapper coursePublishMapper;

    //任務調度入口
    @XxlJob("CoursePublishJobHandler")
    public void coursePublishJobHandler() throws Exception {
        // 分片參數
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        log.debug("shardIndex=" + shardIndex + ",shardTotal=" + shardTotal);
        //參數:分片序號、分片總數、消息類型、一次最多取到的任務數量、一次任務調度執行的超時時間
        process(shardIndex, shardTotal, "course_publish", 30, 60);
    }

    //課程發布任務處理
    @Override
    public boolean execute(MqMessage mqMessage) {
        //從mqMessqge拿到課程id
        String businessKey1 = mqMessage.getBusinessKey1();
        long courseId = Integer.parseInt(businessKey1);
        //課程靜態化
        generateCourseHtml(mqMessage, courseId);
        //課程索引
        saveCourseIndex(mqMessage, courseId);
        //課程緩存
        saveCourseCache(mqMessage, courseId);
        return true;
    }


    //生成課程靜態化頁面並上傳至文件系統
    public void generateCourseHtml(MqMessage mqMessage, long courseId) {

        log.debug("開始進行課程靜態化,課程id:{}", courseId);
        //消息id
        Long id = mqMessage.getId();
        //消息處理的service
        MqMessageService mqMessageService = this.getMqMessageService();
        //消息冪等性處理
        int stageOne = mqMessageService.getStageOne(id);
        if (stageOne > 0) {
            log.debug("課程靜態化已處理直接返回，課程id:{}", courseId);
            return;
        }
        //生成靜態化頁面
        File file = coursePublishService.generateCourseHtml(courseId);
        //上傳靜態化頁面
        if (file != null) {
            coursePublishService.uploadCourseHtml(courseId, file);
        }
        //保存第一階段狀態
        mqMessageService.completedStageOne(id);

    }

    //將課程信息緩存至redis
    public void saveCourseCache(MqMessage mqMessage, long courseId) {
        log.debug("將課程信息緩存至redis,課程id:{}", courseId);
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


    }

    //保存課程索引信息 第二階段任務
    public void saveCourseIndex(MqMessage mqMessage, long courseId) {
        log.debug("保存課程索引信息,課程id:{}", courseId);
        //任務id
        Long taskId = mqMessage.getId();
        MqMessageService mqMessageService = this.getMqMessageService();
        int stageTwo = mqMessageService.getStageTwo(taskId);
        //任務冪等性處理
        if (stageTwo > 0) {
            log.debug("課程信息已寫入，課程id:{}", courseId);
            return;
        }
        //查詢課程信息, 調用搜索服務添加索引
        Boolean result = saveCourseIndex(courseId);
        //保存第二階段狀態
        mqMessageService.completedStageTwo(taskId);
    }

    private Boolean saveCourseIndex(Long courseId) {

        //取出課程發布信息
        CoursePublish coursePublish = coursePublishMapper.selectById(courseId);
        //拷貝至課程索引對象
        CourseIndex courseIndex = new CourseIndex();
        BeanUtils.copyProperties(coursePublish, courseIndex);
        //遠程調用搜索服務api添加課程信息到索引
        Boolean add = searchServiceClient.add(courseIndex);
        if (!add) {
            XueChengPlusException.cast("添加索引失敗");
        }
        return add;
    }
}
