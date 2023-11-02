package com.xuecheng.content;

import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.service.CourseCategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * @description:
 * @author: Ian Wang
 * @date: 2023/10/26 下午 03:18
 * @version: 1.0
 */
@SpringBootTest
class CourseCategoryServiceTests {

    @Autowired
    CourseCategoryMapper courseCategoryMapper;

    @Autowired
    CourseCategoryService courseCategoryService;

    @Test
    List<CourseCategoryTreeDto> pre_testCourseCategoryService() {
        String id = "1";
        List<CourseCategoryTreeDto> courseCategoryTreeDtos = courseCategoryMapper.selectTreeNodes(id);
        System.out.println(courseCategoryTreeDtos);
        Map<String, CourseCategoryTreeDto> tempMap = courseCategoryTreeDtos.stream().filter(CourseCategoryTreeDto->CourseCategoryTreeDto.getId()!=id)
                .collect(Collectors.toMap(key->key.getId(), value->value, (key1, key2)->key2));
        List<CourseCategoryTreeDto> categoryTreeDtos = new ArrayList<>();
        courseCategoryTreeDtos.stream().filter(dto->dto.getId()!=id).forEach(
                item->{
                    //父節點為傳入id時 為第一層
                    if(item.getParentid()==id){
                        categoryTreeDtos.add(item);
                    }
                    //來自map中將子節點組裝上去
                    CourseCategoryTreeDto courseCategoryTreeParentDto = tempMap.get(item.getParentid());
                    if(courseCategoryTreeParentDto!=null){
                        if(courseCategoryTreeParentDto.getChildrenTreeNodes()==null){
                            courseCategoryTreeParentDto.setChildrenTreeNodes(new ArrayList<CourseCategoryTreeDto>());
                        }
                        courseCategoryTreeParentDto.getChildrenTreeNodes().add(item);
                    }
                }
        );
        return categoryTreeDtos;
    }
    
    @Test
    void testCourseCategoryService(){
        List<CourseCategoryTreeDto> courseCategoryTreeDtos = courseCategoryService.queryTreeNodes("1");
        System.out.println(courseCategoryTreeDtos);
    }


}

