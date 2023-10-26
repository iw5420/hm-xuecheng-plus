package com.xuecheng.content.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

/**
 * @description: 課程查詢參數Dto
 * @author: Ian Wang
 * @date: 2023/10/25 下午 04:05
 * @version: 1.0
 */
@Data
@ToString
public class QueryCourseParamsDto {
    //審核狀態
    @ApiModelProperty("審核狀態")
    private String auditStatus;
    //課程名稱
    @ApiModelProperty("課程名稱")
    private String courseName;
    //發布狀態
    @ApiModelProperty("發布狀態")
    private String publishStatus;
}
