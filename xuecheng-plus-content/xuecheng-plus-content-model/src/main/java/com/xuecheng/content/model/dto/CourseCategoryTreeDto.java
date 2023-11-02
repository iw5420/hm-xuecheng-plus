package com.xuecheng.content.model.dto;

import com.xuecheng.content.model.po.CourseCategory;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @description: 課程分類樹型結點dto
 * @author: Ian Wang
 * @date: 2023/10/31 下午 05:07
 * @version: 1.0
 */
@Data
public class CourseCategoryTreeDto extends CourseCategory implements Serializable {

    //子節點
    List<CourseCategoryTreeDto> childrenTreeNodes;
}
