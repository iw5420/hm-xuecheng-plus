package com.xuecheng.base.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

/**
 * @description: 課程查詢參數Dto
 * @author: Ian Wang
 * @date: 2023/10/25 下午 03:52
 * @version: 1.0
 */
@Data
@ToString
public class PageParams {

    //當前頁碼
    @ApiModelProperty("當前頁碼")
    private Long pageNo = 1L;

    //每頁紀錄數默認值
    @ApiModelProperty("每頁紀錄數默認值")
    private Long pageSize = 30L;

    public PageParams() {

    }

    public PageParams(long pageNo, long pageSize) {
        this.pageNo = pageNo;
        this.pageSize = pageSize;
    }
}
