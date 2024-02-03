package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.SaveTeachplanDto;
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
    /*
     * @description 保存課程計劃
     * @param teachplanDto  課程計劃信息
     * @return void
     * @author: Ian Wang
     * @date: 2024/1/18 上午 11:41
     */
    public void saveTeachplan(SaveTeachplanDto teachplanDto);
    /*
     * @description: 刪除課程計劃
     * @param:  課程計劃id
     * @return: void
     * @author: Ian Wang
     * @date: 2024/2/3 下午 07:32
     */
    public void deleteTeachplan(Long teachplanId);
    /*
     * @description: 課程計畫上下移動
     * @param: moveType, teachplanId
     * @return: void
     * @author: Ian Wang
     * @date: 2024/2/3 下午 07:33
     */
    public void orderByTeachplan(String moveType, Long teachplanId);
}
