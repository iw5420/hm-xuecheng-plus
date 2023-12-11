package com.xuecheng.content.model.dto;

import com.xuecheng.base.exception.ValidationGroups;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * @description 添加課程dto
 * @author Mr.M
 * @date 2022/9/7 17:40
 * @version 1.0
 */
@Data
@ApiModel(value="AddCourseDto", description="新增課程基本信息")
public class AddCourseDto {

 @NotEmpty(groups = {ValidationGroups.Insert.class},message = "新增課程名稱不能為空")
 @NotEmpty(groups = {ValidationGroups.Update.class},message = "修改課程名稱不能為空")
 @ApiModelProperty(value = "課程名稱", required = true)
 private String name;

 @NotEmpty(message = "適用人群不能為空")
 @Size(message = "適用人群內容過少",min = 10)
 @ApiModelProperty(value = "適用人群", required = true)
 private String users;

 @ApiModelProperty(value = "課程標簽")
 private String tags;

 @NotEmpty(message = "課程分類不能為空")
 @ApiModelProperty(value = "大分類", required = true)
 private String mt;

 @NotEmpty(message = "課程分類不能為空")
 @ApiModelProperty(value = "小分類", required = true)
 private String st;

 @NotEmpty(message = "課程等級不能為空")
 @ApiModelProperty(value = "課程等級", required = true)
 private String grade;

 @ApiModelProperty(value = "教學模式（普通，錄播，直播等）", required = true)
 private String teachmode;

 @ApiModelProperty(value = "課程介紹")
 @Size(message = "課程描述內容過少",min = 10)
 private String description;

 @ApiModelProperty(value = "課程圖片", required = true)
 private String pic;

 @NotEmpty(message = "收費規則不能為空")
 @ApiModelProperty(value = "收費規則，對應數據字典", required = true)
 private String charge;

 @ApiModelProperty(value = "價格")
 private Float price;
 @ApiModelProperty(value = "原價")
 private Float originalPrice;


 @ApiModelProperty(value = "qq")
 private String qq;

 @ApiModelProperty(value = "微信")
 private String wechat;
 @ApiModelProperty(value = "電話")
 private String phone;

 @ApiModelProperty(value = "有效期")
 private Integer validDays;
}
