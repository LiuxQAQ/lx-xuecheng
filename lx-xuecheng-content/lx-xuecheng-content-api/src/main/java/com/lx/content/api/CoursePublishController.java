package com.lx.content.api;

import com.lx.content.model.dto.CoursePreviewDto;
import com.lx.content.service.CoursePublishService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author lx
 * @date 2023/5/26 22:33
 * @description 课程预览，发布
 */
@Api(value = "课程预览发布接口",tags = "课程预览发布接口")
@Controller // 可以响应页面  RestController是响应json的
public class CoursePublishController {

    private static final Long COMPANY_ID = 1232141425L;

    @Autowired
    private CoursePublishService coursePublishService;

    @GetMapping("/coursepreview/{courseId}")
    @ApiOperation("获取课程预览页面")
    public ModelAndView preview(@PathVariable("courseId") Long courseId){

        // 获取课程预览信息
        CoursePreviewDto coursePreviewInfo = coursePublishService.getCoursePreviewInfo(courseId);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("model",coursePreviewInfo);
        modelAndView.setViewName("course_template");
        return modelAndView;
    }

    @PostMapping("/courseaudit/commit/{courseId}")
    @ResponseBody
    @ApiOperation("提交审核接口")
    public void commitAudit(@PathVariable("courseId") Long courseId){
        coursePublishService.commitAudit(COMPANY_ID,courseId);
    }

    @ApiOperation("课程发布")
    @ResponseBody
    @PostMapping("/coursepublish/{courseId}")
    public void coursePublish(@PathVariable("courseId") Long courseId){
        coursePublishService.publish(COMPANY_ID,courseId);
    }

}
