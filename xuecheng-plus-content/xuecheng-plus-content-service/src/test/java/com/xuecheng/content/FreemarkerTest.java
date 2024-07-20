package com.xuecheng.content;

import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.service.CoursePublishService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Mr.M
 * @version 1.0
 * @description freemarker測試
 * @date 2022/9/20 18:42
 */
@SpringBootTest
public class FreemarkerTest {

    @Autowired
    CoursePublishService coursePublishService;


    //測試頁面靜態化
    @Test
    public void testGenerateHtmlByTemplate() throws IOException, TemplateException {
        //配置freemarker
        Configuration configuration = new Configuration(Configuration.getVersion());

        //加載模板
        //選指定模板路徑,classpath下templates下
        //得到classpath路徑
        String classpath = this.getClass().getResource("/").getPath();
        System.out.println("classpath : " + classpath);
        configuration.setDirectoryForTemplateLoading(new File(classpath + "/templates/"));
        //設置字符編碼
        configuration.setDefaultEncoding("utf-8");

        //指定模板文件名稱
        Template template = configuration.getTemplate("course_template.ftl");

        //準備數據
        CoursePreviewDto coursePreviewInfo = coursePublishService.getCoursePreviewInfo(2L);

        Map<String, Object> map = new HashMap<>();
        map.put("model", coursePreviewInfo);

        //靜態化
        //參數1：模板，參數2：數據模型
        String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);
        System.out.println(content);
        //將靜態化內容輸出到文件中
        InputStream inputStream = IOUtils.toInputStream(content);
        //輸出流
        FileOutputStream outputStream = new FileOutputStream("D:\\develop\\freemarker_test\\test.html");
        IOUtils.copy(inputStream, outputStream);

    }

}
