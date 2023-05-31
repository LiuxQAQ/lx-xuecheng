package com.lx.content.api;

import com.lx.base.execption.ValidationGroups;
import com.lx.base.model.PageParams;
import com.lx.base.model.PageResult;
import com.lx.content.model.dto.AddCourseDto;
import com.lx.content.model.dto.CourseBaseInfoDto;
import com.lx.content.model.dto.EditCourseDto;
import com.lx.content.model.dto.QueryCourseParamsDto;
import com.lx.content.model.entity.CourseBase;
import com.lx.content.service.CourseBaseInfoService;
import com.lx.content.utils.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Api(value = "课程信息编辑接口",tags = "课程信息编辑接口")
public class CourseBaseInfoController {

    @Autowired
    private CourseBaseInfoService courseBaseInfoService;

    @PostMapping("/course/list")
    @ApiOperation("课程查询接口")
    public PageResult<CourseBase> list(PageParams pageParams,
                                       @RequestBody(required = false) QueryCourseParamsDto queryCourseParams){
        return courseBaseInfoService.queryCourseBaseList(pageParams, queryCourseParams);
    }

    @PostMapping("/course")
    @ApiOperation("新增课程基础信息")
    // 添加@Validated注解 如果校验出错Spring会抛出MethodArgumentNotValidException异常，我们需要在统一异常处理器中捕获异常
    // {ValidationGroups.Insert.class} 指定使用的分组名
    // 如果包下的校验规则满足不了需求可以自定义规则注解  --查阅资料
    public CourseBaseInfoDto createCourseBase(@RequestBody @Validated({ValidationGroups.Insert.class}) AddCourseDto addCourseDto){
        // 机构id，由于认证系统没有上线暂时硬编码
        Long companyId = 1232141425L;
        return courseBaseInfoService.createCourseBase(companyId,addCourseDto);
    }

    @GetMapping("/course/{courseId}")
    @ApiOperation("根据课程id查询")
    public CourseBaseInfoDto getCourseBaseById(@PathVariable("courseId") Long courseId){
        // 取出当前用户身份
//        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        SecurityUtil.XcUser user = SecurityUtil.getUser();
        System.out.println(user);

        return courseBaseInfoService.getCourseBaseInfo(courseId);
    }

    @PutMapping("/course")
    @ApiOperation("修改课程基础信息")
    public CourseBaseInfoDto modifyCourseBase(@RequestBody @Validated EditCourseDto editCourseDto){
        Long companyId = 1232141425L;
        return courseBaseInfoService.updateCourseBase(companyId,editCourseDto);
    }

    @DeleteMapping("/course/{courseId}")
    public void removeCourseBaseInfo(@PathVariable("courseId") Long courseId){
        Long companyId = 1232141425L;
        courseBaseInfoService.removeCourseBaseInfo(companyId,courseId);
    }


}
