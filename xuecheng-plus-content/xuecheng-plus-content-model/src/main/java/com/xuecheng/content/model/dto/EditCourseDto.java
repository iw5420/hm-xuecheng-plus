package com.xuecheng.content.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @description:
 * @author: Ian Wang
 * @date: 2023/12/14 下午 06:03
 * @version: 1.0
 */
@Data
public class EditCourseDto extends AddCourseDto{
    @ApiModelProperty(value = "課程id", required = true)
    private Long id;
}
