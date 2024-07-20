package com.xuecheng.content.model.dto;

import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @description 課程預覽數據模型
 * @author Ian
 * @date 2024/7/10
 * @version 1.0
 */
@Data
@ToString
public class CoursePreviewDto {

    //課程基本信息,課程營銷信息
    CourseBaseInfoDto courseBase;


    //課程計劃信息
    List<TeachplanDto> teachplans;

    //師資信息暫時不加...


}
