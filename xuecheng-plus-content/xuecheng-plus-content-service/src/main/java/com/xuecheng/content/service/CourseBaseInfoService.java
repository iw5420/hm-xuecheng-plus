package com.xuecheng.content.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;

/**
 * @description: 課程信息管理接口
 * @author: Ian Wang
 * @date: 2023/10/26 下午 05:01
 * @version: 1.0
 */
public interface CourseBaseInfoService {
    /*
     * @description 課程查詢接口
     * @param pageParams 分頁參數
     * @param queryCourseParamsDto 查詢條件
     * @return com.xuecheng.base.model.PageResult<com.xuecheng.content.model.po.CourseBase>
     * @author Ian Wang
     * @date: 2023/10/26 下午 05:01
     */
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto);

    /*
     * @description 添加課程基本信息
     * @param companyId  教學機構id
     * @param addCourseDto  課程基本信息
     * @return: com.xuecheng.content.model.dto.CourseBaseInfoDto
     * @author: Ian Wang
     * @date: 2023/11/11 上午 10:17
     */
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto addCourseDto);
    
    /*
     * @description:根據課程id查詢課程信息
     * @param courseId 課程id
     * @return 課程詳細信息
     * @author: Ian Wang
     * @date: 2023/12/14 下午 04:43
     */
    public CourseBaseInfoDto getCourseBaseInfo(Long courseId);
    
    /*
     * @description: 修改課程
     * @param: companyId 機構id
     * @return: editCourseDto 修改課程信息
     * @author: Ian Wang
     * @date: 2023/12/14 下午 06:10
     */
    public CourseBaseInfoDto updateCourseBase(Long companyId, EditCourseDto editCourseDto);
}
