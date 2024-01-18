package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.TeachplanDto;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @description: 課程基本信息管理業務接口
 * @author: Ian Wang
 * @date: 2024/1/16 下午 05:53
 * @version: 1.0
 */
public interface TeachplanService {
/*
 * @description 查詢課程計劃樹型結構
 * @param courseId  課程id
 * @return List<TeachplanDto>
 * @author: Ian Wang
 * @date: 2024/1/16 下午 05:55
 */
    public List<TeachplanDto> findTeachplanTree(long courseId);
}
