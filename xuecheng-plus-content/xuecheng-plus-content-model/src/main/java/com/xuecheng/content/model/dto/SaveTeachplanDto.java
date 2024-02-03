package com.xuecheng.content.model.dto;

import lombok.Data;
import lombok.ToString;

/**
 * @description: 保存課程計劃dto，包括新增、修改
 * @author: Ian Wang
 * @date: 2024/1/18 上午 11:26
 * @version: 1.0
 */
@Data
@ToString
public class SaveTeachplanDto {
    /***
     * 教學計劃id
     */
    private Long id;

    /**
     * 課程計劃名稱
     */
    private String pname;

    /**
     * 課程計劃父級Id
     */
    private Long parentid;

    /**
     * 層級，分為1、2、3級
     */
    private Integer grade;

    /**
     * 課程類型:1視頻、2文檔
     */
    private String mediaType;


    /**
     * 課程標識
     */
    private Long courseId;

    /**
     * 課程發布標識
     */
    private Long coursePubId;


    /**
     * 是否支持試學或預覽（試看）
     */
    private String isPreview;

}
