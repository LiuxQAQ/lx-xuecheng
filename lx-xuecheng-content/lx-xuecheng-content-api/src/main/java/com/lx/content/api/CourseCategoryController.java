package com.lx.content.api;

import com.lx.content.model.dto.CourseCategoryTreeDto;
import com.lx.content.service.CourseCategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Api(value = "课程类别获取接口",tags = "课程类别获取接口")
public class CourseCategoryController {

    public static final String COURSE_CATEGORY_ROOT = "1";

    @Autowired
    private CourseCategoryService courseCategoryService;

    @GetMapping("/course-category/tree-nodes")
    @ApiOperation("获取课程类别-构建类别树接口")
    public List<CourseCategoryTreeDto> queryTreeNodes(){
        return courseCategoryService.queryTreeNodes(COURSE_CATEGORY_ROOT);
    }


}
