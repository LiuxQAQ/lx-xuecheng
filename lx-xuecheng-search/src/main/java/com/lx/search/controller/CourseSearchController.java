package com.lx.search.controller;

import com.lx.base.model.PageParams;
import com.lx.base.model.PageResult;
import com.lx.search.model.dto.SearchCourseParamDto;
import com.lx.search.model.entity.CourseIndex;
import com.lx.search.service.CourseSearchService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(value = "课程搜索接口",tags = "课程搜索接口")
@RestController
@RequestMapping("/course")
public class CourseSearchController {

    @Autowired
    private CourseSearchService courseSearchService;

    @ApiOperation("课程搜索列表")
    @GetMapping("/list")
    public PageResult<CourseIndex> list(PageParams pageParams, SearchCourseParamDto searchCourseParamDto){
        return courseSearchService.queryCoursePubIndex(pageParams,searchCourseParamDto);
    }
}
