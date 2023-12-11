package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseCategory;
import com.xuecheng.content.model.po.CourseMarket;
import com.xuecheng.content.service.CourseBaseInfoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @description:
 * @author: Ian Wang
 * @date: 2023/10/26 下午 05:16
 * @version: 1.0
 */
@Service
public class CourseBaseInfoServiceImpl implements CourseBaseInfoService {

    @Autowired
    CourseBaseMapper courseBaseMapper;

    @Autowired
    CourseMarketMapper courseMarketMapper;

    @Autowired
    CourseCategoryMapper courseCategoryMapper;

    @Override
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto courseParamsDto) {
        //拼裝查詢條件
        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();
        //根據名稱模糊查詢,在sql中拼接 course_base.name like '%值%'
        queryWrapper.like(StringUtils.isNotEmpty(courseParamsDto.getCourseName()),CourseBase::getName,courseParamsDto.getCourseName());
        //根據課程審核狀態查詢 course_base.audit_status = ?
        queryWrapper.eq(StringUtils.isNotEmpty(courseParamsDto.getAuditStatus()), CourseBase::getAuditStatus,courseParamsDto.getAuditStatus());
        //todo:按課程發布狀態查詢

        //創建page分頁參數對象，參數:當前頁碼，每頁紀錄數
        Page<CourseBase> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        //開始進行分頁查詢
        Page<CourseBase> pageResult = courseBaseMapper.selectPage(page, queryWrapper);
        //數據列表
        List<CourseBase> items = pageResult.getRecords();
        //總紀錄數
        long total = pageResult.getTotal();

        //List<T> items, long counts, long page, long pageSize
        PageResult<CourseBase> courseBasePageResult = new PageResult<CourseBase>(items,total,pageParams.getPageNo(), pageParams.getPageSize());
        return  courseBasePageResult;
    }

    @Transactional
    @Override
    public CourseBaseInfoDto createCourseBase(Long companyId,AddCourseDto dto) {

        //合法性校驗
//        if (StringUtils.isBlank(dto.getName())) {
//            throw new XueChengPlusException("課程名稱為空");
//        }
//        if (StringUtils.isBlank(dto.getMt())) {
//            throw new XueChengPlusException("課程分類為空");
//        }
//        if (StringUtils.isBlank(dto.getSt())) {
//            throw new XueChengPlusException("課程分類為空");
//        }
//        if (StringUtils.isBlank(dto.getGrade())) {
//            throw new XueChengPlusException("課程等級為空");
//        }
//        if (StringUtils.isBlank(dto.getTeachmode())) {
//            throw new XueChengPlusException("教育模式為空");
//        }
//        if (StringUtils.isBlank(dto.getUsers())) {
//            throw new XueChengPlusException("適應人群");
//        }
//        if (StringUtils.isBlank(dto.getCharge())) {
//            throw new XueChengPlusException("收費規則為空");
//        }

        //新增對象
        CourseBase courseBaseNew = new CourseBase();
        //將填寫的課程信息賦值給新增對象
        BeanUtils.copyProperties(dto,courseBaseNew);
        //設置審核狀態 未提交
        courseBaseNew.setAuditStatus("202002");
        //設置發布狀態 未發布
        courseBaseNew.setStatus("203001");
        //機構id
        courseBaseNew.setCompanyId(companyId);
        //添加時間
        courseBaseNew.setCreateDate(LocalDateTime.now());
        //插入課程基本信息表
        int insert = courseBaseMapper.insert(courseBaseNew);
        if(insert<=0){
            throw new XueChengPlusException("新增課程基本信息失敗");
        }
        //向課程營銷表保存課程營銷信息
        //課程營銷信息
        CourseMarket courseMarketNew = new CourseMarket();
        Long courseId = courseBaseNew.getId();
        BeanUtils.copyProperties(dto,courseMarketNew);
        courseMarketNew.setId(courseId);
        int i = saveCourseMarket(courseMarketNew);
        if(i<=0){
            throw new XueChengPlusException("保存課程營銷信息失敗");
        }
        //查詢課程基本信息及營銷信息並返回
        return getCourseBaseInfo(courseId);
    }

    //保存課程營銷信息
    private int saveCourseMarket(CourseMarket courseMarketNew){
        //收費規則
        String charge = courseMarketNew.getCharge();
        if(StringUtils.isBlank(charge)){
            throw new XueChengPlusException("收費規則沒有選擇");
        }
        //收費規則為收費
        if(charge.equals("201001")){
            if(courseMarketNew.getPrice() == null || courseMarketNew.getPrice().floatValue()<=0){
                throw new XueChengPlusException("課程為收費價格不能為空且必須大於0");
            }
        }
        //根據id從課程營銷表查詢
        CourseMarket courseMarketObj = courseMarketMapper.selectById(courseMarketNew.getId());
        if(courseMarketObj == null){
            return courseMarketMapper.insert(courseMarketNew);
        }else{
            BeanUtils.copyProperties(courseMarketNew,courseMarketObj);
            courseMarketObj.setId(courseMarketNew.getId());
            return courseMarketMapper.updateById(courseMarketObj);
        }
    }

    //根據課程id查詢課程基本信息，包括基本信息和營銷信息
    public CourseBaseInfoDto getCourseBaseInfo(long courseId){

        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if(courseBase == null){
            return null;
        }
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(courseBase,courseBaseInfoDto);
        if(courseMarket != null){
            BeanUtils.copyProperties(courseMarket,courseBaseInfoDto);
        }

        //查詢分類名稱 (因為原始資料只有節點沒有名稱)
        CourseCategory courseCategoryBySt = courseCategoryMapper.selectById(courseBase.getSt());
        courseBaseInfoDto.setStName(courseCategoryBySt.getName());
        CourseCategory courseCategoryByMt = courseCategoryMapper.selectById(courseBase.getMt());
        courseBaseInfoDto.setMtName(courseCategoryByMt.getName());

        return courseBaseInfoDto;

    }


}
