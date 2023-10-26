package com.xuecheng.base.model;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * @description: 分頁查詢結果模型類
 * @author: Ian Wang
 * @date: 2023/10/25 下午 03:53
 * @version: 1.0
 */
@Data
@ToString
public class PageResult<T> implements Serializable {

    //數據列表
    private List<T> items;

    //總紀錄數
    private long counts;

    //當前頁碼
    private long page;

    //每頁紀錄數
    private long pageSize;

    public PageResult(List<T> items, long counts, long page, long pageSize) {
        this.items = items;
        this.counts = counts;
        this.page = page;
        this.pageSize = pageSize;
    }

}

