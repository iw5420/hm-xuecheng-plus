package com.xuecheng.content.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xuecheng.base.exception.CommonError;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.config.MultipartSupportConfig;
import com.xuecheng.content.feignclient.MediaServiceClient;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.mapper.CoursePublishMapper;
import com.xuecheng.content.mapper.CoursePublishPreMapper;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseMarket;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.content.model.po.CoursePublishPre;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.content.service.TeachplanService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MqMessageService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @description TODO
 * @author Ian
 * @date 2024/7/10
 * @version 1.0
 */
@Slf4j
@Service
public class CoursePublishServiceImpl implements CoursePublishService {

    @Autowired
    CourseBaseInfoService courseBaseInfoService;

    @Autowired
    TeachplanService teachplanService;

    @Autowired
    CourseBaseMapper courseBaseMapper;

    @Autowired
    CourseMarketMapper courseMarketMapper;

    @Autowired
    CoursePublishPreMapper coursePublishPreMapper;

    @Autowired
    CoursePublishMapper coursePublishMapper;

    @Autowired
    MqMessageService mqMessageService;

    @Autowired
    MediaServiceClient mediaServiceClient;

    @Autowired
    RedisTemplate redisTemplate;

    @Override
    public CoursePreviewDto getCoursePreviewInfo(Long courseId) {

        //課程基本信息、營銷信息
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(courseId);

        //課程計劃信息
        List<TeachplanDto> teachplanTree= teachplanService.findTeachplanTree(courseId);

        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();
        coursePreviewDto.setCourseBase(courseBaseInfo);
        coursePreviewDto.setTeachplans(teachplanTree);
        return coursePreviewDto;
    }

    @Transactional
    @Override
    public void commitAudit(Long companyId, Long courseId) {

        //約束校驗
        //課程基本信息
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(courseId);
        //課程審核狀態
        String auditStatus = courseBaseInfo.getAuditStatus();
        //當前審核狀態為已提交不允許再次提交
        if("202003".equals(auditStatus)){
            XueChengPlusException.cast("當前為等待審核狀態，審核完成可以再次提交。");
        }
        //本機構只允許提交本機構的課程
        if(!courseBaseInfo.getCompanyId().equals(companyId)){
            XueChengPlusException.cast("不允許提交其它機構的課程。");
        }

        //課程圖片是否填寫
        if(StringUtils.isEmpty(courseBaseInfo.getPic())){
            XueChengPlusException.cast("提交失敗，請上傳課程圖片");
        }
        //添加課程預發布記錄
        CoursePublishPre coursePublishPre = new CoursePublishPre();
        //加部分營銷信息
        BeanUtils.copyProperties(courseBaseInfo,coursePublishPre);
        //課程營銷信息
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        //轉為json
        String courseMarketJson = JSON.toJSONString(courseMarket);
        //將課程營銷信息json數據放入課程預發布表
        coursePublishPre.setMarket(courseMarketJson);

        //查詢課程計劃信息
        List<TeachplanDto> teachplanTree = teachplanService.findTeachplanTree(courseId);
        if(teachplanTree.size()<=0){
            XueChengPlusException.cast("提交失敗，還沒有添加課程計劃");
        }
        //轉json
        String teachplanTreeString = JSON.toJSONString(teachplanTree);
        coursePublishPre.setTeachplan(teachplanTreeString);

        //設置預發布記錄狀態,已提交
        coursePublishPre.setStatus("202003");
        //教學機構id
        coursePublishPre.setCompanyId(companyId);
        //提交時間
        coursePublishPre.setCreateDate(LocalDateTime.now());
        CoursePublishPre coursePublishPreUpdate = coursePublishPreMapper.selectById(courseId);
        if(coursePublishPreUpdate == null){
            //添加課程預發布記錄
            coursePublishPreMapper.insert(coursePublishPre);
        }else{
            coursePublishPreMapper.updateById(coursePublishPre);
        }

        //更新課程基本表的審核狀態
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        courseBase.setAuditStatus("202003");
        courseBaseMapper.updateById(courseBase);
    }

    @Transactional
    @Override
    public void publish(Long companyId, Long courseId) {

        //約束校驗
        //查詢課程預發布表
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        if(coursePublishPre == null){
            XueChengPlusException.cast("請先提交課程審核，審核通過才可以發布");
        }
        //本機構只允許提交本機構的課程
        if(!coursePublishPre.getCompanyId().equals(companyId)){
            XueChengPlusException.cast("不允許提交其它機構的課程。");
        }


        //課程審核狀態
        String auditStatus = coursePublishPre.getStatus();
        //審核通過方可發布
        if(!"202004".equals(auditStatus)){
            XueChengPlusException.cast("操作失敗，課程審核通過方可發布。");
        }

        //保存課程發布信息
        saveCoursePublish(courseId);

        //保存消息表
        saveCoursePublishMessage(courseId);

        //刪除課程預發布表對應記錄
        coursePublishPreMapper.deleteById(courseId);

    }

    @Override
    public File generateCourseHtml(Long courseId) {

        //靜態化文件
        File htmlFile  = null;

        try {
            //配置freemarker
            Configuration configuration = new Configuration(Configuration.getVersion());

            //加載模板
            //選指定模板路徑,classpath下templates下
            //得到classpath路徑
            String classpath = this.getClass().getResource("/").getPath();
            configuration.setDirectoryForTemplateLoading(new File(classpath + "/templates/"));
            //設置字符編碼
            configuration.setDefaultEncoding("utf-8");

            //指定模板文件名稱
            Template template = configuration.getTemplate("course_template.ftl");

            //準備數據
            CoursePreviewDto coursePreviewInfo = this.getCoursePreviewInfo(courseId);

            Map<String, Object> map = new HashMap<>();
            map.put("model", coursePreviewInfo);

            //靜態化
            //參數1：模板，參數2：數據模型
            String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);
//            System.out.println(content);
            //將靜態化內容輸出到文件中
            InputStream inputStream = IOUtils.toInputStream(content);
            //創建靜態化文件
            htmlFile = File.createTempFile("course",".html");
            log.debug("課程靜態化，生成靜態文件:{}",htmlFile.getAbsolutePath());
            //輸出流
            FileOutputStream outputStream = new FileOutputStream(htmlFile);
            IOUtils.copy(inputStream, outputStream);
        } catch (Exception e) {
            log.error("課程靜態化異常:{}",e.toString());
            XueChengPlusException.cast("課程靜態化異常");
        }

        return htmlFile;
    }

    @Override
    public void uploadCourseHtml(Long courseId, File file) {
        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(file);
        String course = mediaServiceClient.uploadFile(multipartFile, "course/"+courseId+".html");
        if(course==null){
            XueChengPlusException.cast("上傳靜態文件異常");
        }
    }

    @Override
    public CoursePublish getCoursePublish(Long courseId){
        log.info("從數據庫查");
        CoursePublish coursePublish = coursePublishMapper.selectById(courseId);
        return coursePublish ;
    }

    /**
     * @description 保存課程發布信息
     * @param courseId  課程id
     * @return void
     * @author Ian
     * @date 2024/7/12
     */
    private void saveCoursePublish(Long courseId){
        //整合課程發布信息
        //查詢課程預發布表
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        if(coursePublishPre == null){
            XueChengPlusException.cast("課程預發布數據為空");
        }

        CoursePublish coursePublish = new CoursePublish();

        //拷貝到課程發布對象
        BeanUtils.copyProperties(coursePublishPre,coursePublish);
        coursePublish.setStatus("203002");
        CoursePublish coursePublishUpdate = coursePublishMapper.selectById(courseId);
        if(coursePublishUpdate == null){
            coursePublishMapper.insert(coursePublish);
        }else{
            coursePublishMapper.updateById(coursePublish);
        }
        //更新課程基本表的發布狀態
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        courseBase.setStatus("203002");
        courseBaseMapper.updateById(courseBase);

    }

    /**
     * @description 保存消息表記錄，稍後實現
     * @param courseId  課程id
     * @return void
     * @author Ian
     * @date 2024/7/12
     */
    private void saveCoursePublishMessage(Long courseId){
        MqMessage mqMessage = mqMessageService.addMessage("course_publish", String.valueOf(courseId), null, null);
        if(mqMessage==null){
            XueChengPlusException.cast(CommonError.UNKOWN_ERROR);
        }
    }

    @Override
    public CoursePublish getCoursePublishCache(Long courseId) {
        //查詢緩存
        Object jsonObj = redisTemplate.opsForValue().get("course:" + courseId);
        if(jsonObj!=null){
            String jsonString = jsonObj.toString();
            log.info("從緩存查");
            if ("null".equals(jsonString)) {
                return  null;
            }
            CoursePublish coursePublish = JSONObject.parseObject(jsonString, CoursePublish.class);
            return coursePublish;
        }else{
            log.info("從數據庫查詢");
            CoursePublish coursePublish = getCoursePublish(courseId);
            //if(coursePublish!=null){
            redisTemplate.opsForValue().set("course:"+courseId, JSON.toJSONString(coursePublish), 30 + new Random().nextInt(100), TimeUnit.SECONDS);
            //}
            return coursePublish;
        }
    }


}
