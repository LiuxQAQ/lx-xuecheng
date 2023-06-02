package com.lx.learning.api;


import com.lx.base.execption.XueChengException;
import com.lx.base.model.PageResult;
import com.lx.content.model.entity.CoursePublish;
import com.lx.learning.feignclient.ContentServiceClient;
import com.lx.learning.model.dto.MyCourseTableParams;
import com.lx.learning.model.dto.XcChooseCourseDto;
import com.lx.learning.model.dto.XcCourseTablesDto;
import com.lx.learning.model.entity.XcChooseCourse;
import com.lx.learning.model.entity.XcCourseTables;
import com.lx.learning.service.MyCourseTableService;
import com.lx.learning.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author lx
 * @date 2023/5/31 22:52
 * @description 我的课程表接口
 */
@Api(value = "我的课程表接口",tags = "我的课程表接口")
@Slf4j
@RestController
public class MyCourseTableController {

    @Autowired
    private MyCourseTableService courseTableService;

    @ApiOperation("添加选课")
    @PostMapping("/choosecourse/{courseId}")
    public XcChooseCourseDto addChooseCourse(@PathVariable("courseId") Long courseId){
        // 登录用户
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        if (user==null){
            XueChengException.cast("请登录后选课");
        }
        String userId = user.getId();
        return courseTableService.addChooseCourse(userId,courseId);
    }

    @ApiOperation("查询学习资格")
    @PostMapping("/choosecourse/learnstatus/{courseId}")
    public XcCourseTablesDto getLearningStatus(@PathVariable("courseId") Long courseId){
        // 登录用户
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        if (user==null){
            XueChengException.cast("请登陆后选课");
        }
        String userId = user.getId();
        return courseTableService.getLearningStatus(userId,courseId);
    }

    @ApiOperation("我的课程表")
    @GetMapping("/mycoursetable")
    public PageResult<XcCourseTables> mycoursetable(MyCourseTableParams params) {
        //登录用户
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        if(user == null){
            XueChengException.cast("请登录后继续选课");
        }
        String userId = user.getId();
        //设置当前的登录用户
        params.setUserId(userId);
        return courseTableService.mycourestabls(params);
    }

}
