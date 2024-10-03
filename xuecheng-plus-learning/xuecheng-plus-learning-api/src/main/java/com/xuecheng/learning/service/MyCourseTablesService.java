package com.xuecheng.learning.service;


import com.xuecheng.base.model.PageResult;
import com.xuecheng.learning.model.dto.MyCourseTableParams;
import com.xuecheng.learning.model.dto.XcChooseCourseDto;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;
import com.xuecheng.learning.model.po.XcCourseTables;

/**
 * @description 我的課程表service接口
 * @author Ian
 * @date 2024/8/2
 * @version 1.0
 */
public interface MyCourseTablesService {

    /**
     * @description 添加選課
     * @param userId 用戶id
     * @param courseId 課程id
     * @return com.xuecheng.learning.model.dto.XcChooseCourseDto
     * @author Ian
     * @date 2024/8/2
     */
    public XcChooseCourseDto addChooseCourse(String userId, Long courseId);

    /**
     * @description 判斷學習資格
     * @param userId
     * @param courseId
     * @return XcCourseTablesDto 學習資格狀態 [{"code":"702001","desc":"正常學習"},{"code":"702002","desc":"沒有選課或選課後沒有支付"},{"code":"702003","desc":"已過期需要申請續期或重新支付"}]
     * @author Ian
     * @date 2024/8/2
     */
    public XcCourseTablesDto getLearningStatus(String userId, Long courseId);

    /**
     * @description 我的课程表
     * @param params
     * @return com.xuecheng.base.model.PageResult<com.xuecheng.learning.model.po.XcCourseTables>
     * @author Mr.M
     * @date 2022/10/27 9:24
     */
    public PageResult<XcCourseTables> mycourestabls(MyCourseTableParams params);
}
