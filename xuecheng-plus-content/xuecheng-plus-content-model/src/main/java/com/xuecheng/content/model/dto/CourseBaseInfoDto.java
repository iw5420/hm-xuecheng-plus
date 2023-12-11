package com.xuecheng.content.model.dto;

import com.xuecheng.content.model.po.CourseBase;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @description 課程基本信息dto
 * @author Mr.M
 * @date 2022/9/7 17:44
 * @version 1.0
 */
@Data
public class CourseBaseInfoDto extends CourseBase {


 /**
  * 收費規則，對應數據字典
  */
 private String charge;

 /**
  * 價格
  */
 private Float price;


 /**
  * 原價
  */
 private Float originalPrice;

 /**
  * 咨詢qq
  */
 private String qq;

 /**
  * 微信
  */
 private String wechat;

 /**
  * 電話
  */
 private String phone;

 /**
  * 有效期天數
  */
 private Integer validDays;

 /**
  * 大分類名稱
  */
 private String mtName;

 /**
  * 小分類名稱
  */
 private String stName;

}