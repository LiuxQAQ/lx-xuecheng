package com.lx.content.service.impl;

import com.lx.content.mapper.CourseBaseMapper;
import com.lx.content.model.dto.CourseBaseInfoDto;
import com.lx.content.model.dto.CoursePreviewDto;
import com.lx.content.model.dto.TeachplanDto;
import com.lx.content.model.entity.TeachplanMedia;
import com.lx.content.service.CourseBaseInfoService;
import com.lx.content.service.CoursePublishService;
import com.lx.content.service.TeachplanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CoursePublishServiceImpl implements CoursePublishService {

    @Autowired
    private CourseBaseInfoService courseBaseInfoService;
    @Autowired
    private TeachplanService teachplanService;
    @Override
    public CoursePreviewDto getCoursePreviewInfo(Long courseId) {

        // 课程基本信息、营销信息
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(courseId);

        // 课程计划信息
        List<TeachplanDto> teachplanTree = teachplanService.findTeachplanTree(courseId);

        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();
        coursePreviewDto.setCourseBase(courseBaseInfo);
        coursePreviewDto.setTeachplans(teachplanTree);
        return coursePreviewDto;

    }
}
