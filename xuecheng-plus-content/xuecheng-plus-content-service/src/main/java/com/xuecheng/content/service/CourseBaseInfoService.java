package com.xuecheng.content.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
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
    PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto);

}
