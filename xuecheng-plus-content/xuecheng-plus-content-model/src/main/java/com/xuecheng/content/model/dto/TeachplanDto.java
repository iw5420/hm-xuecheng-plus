package com.xuecheng.content.model.dto;

import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import lombok.Data;

import java.util.List;

/**
 * @description: 課程計劃樹型結構dto
 * @author: Ian Wang
 * @date: 2023/12/30 下午 01:59
 * @version: 1.0
 */
@Data
public class TeachplanDto extends Teachplan {

    //課程計劃關聯的媒資信息
    TeachplanMedia teachplanMedia;

    //子結點
    List<TeachplanDto> teachPlanTreeNodes;

}