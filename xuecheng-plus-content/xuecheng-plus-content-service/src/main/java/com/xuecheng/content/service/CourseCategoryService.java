package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CourseCategoryTreeDto;

import java.util.List;

/**
 * @description:
 * @author: Ian Wang
 * @date: 2023/11/1 下午 04:57
 * @version: 1.0
 */
public interface CourseCategoryService {
    /**
     * 課程分類樹形結構查詢
     *
     * @return
     */
    public List<CourseCategoryTreeDto> queryTreeNodes(String id);

}
