package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CoursePreviewDto;

import java.io.File;

/**
 * @description 課程預覽、發布接口
 * @author Ian
 * @date 2024/7/10
 * @version 1.0
 */
public interface CoursePublishService {


    /**
     * @description 獲取課程預覽信息
     * @param courseId 課程id
     * @return com.xuecheng.content.model.dto.CoursePreviewDto
     * @author Ian
     * @date 2024/7/10
     */
    public CoursePreviewDto getCoursePreviewInfo(Long courseId);

    /**
     * @description 提交審核
     * @param courseId  課程id
     * @return void
     * @author Ian
     * @date 2024/7/12
     */
    public void commitAudit(Long companyId,Long courseId);

    /**
     * @description 課程發布接口
     * @param companyId 機構id
     * @param courseId 課程id
     * @return void
     * @author Ian
     * @date 2024/7/12
     */
    public void publish(Long companyId,Long courseId);

    /**
     * @description 課程靜態化
     * @param courseId  課程id
     * @return File 靜態化文件
     * @author Mr.M
     * @date 2022/9/23 16:59
     */
    public File generateCourseHtml(Long courseId);
    /**
     * @description 上傳課程靜態化頁面
     * @param file  靜態化文件
     * @return void
     * @author Mr.M
     * @date 2022/9/23 16:59
     */
    public void  uploadCourseHtml(Long courseId,File file);


}
