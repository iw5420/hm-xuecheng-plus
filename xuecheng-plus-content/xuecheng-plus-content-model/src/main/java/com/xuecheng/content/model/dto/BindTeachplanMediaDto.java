package com.xuecheng.content.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value="BindTeachplanMediaDto", description="教學計劃-媒資綁定提交數據")
public class BindTeachplanMediaDto {

    @ApiModelProperty(value = "媒資文件id", required = true)
    private String mediaId;

    @ApiModelProperty(value = "媒資文件名稱", required = true)
    private String fileName;

    @ApiModelProperty(value = "課程計劃標識", required = true)
    private Long teachplanId;


}

