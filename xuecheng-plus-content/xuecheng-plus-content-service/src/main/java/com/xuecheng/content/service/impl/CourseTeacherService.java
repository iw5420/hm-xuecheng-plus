package com.xuecheng.content.service.impl;

import com.xuecheng.content.model.po.CourseTeacher;

import java.util.List;

/**
 * @description:
 * @author: Ian Wang
 * @date: 2024/2/3 下午 11:31
 * @version: 1.0
 */
public interface CourseTeacherService {
    List<CourseTeacher> getCourseTeacherList(Long courseId);

    CourseTeacher saveCourseTeacher(CourseTeacher courseTeacher);

    void deleteCourseTeacher(Long courseId, Long teacherId);
}
