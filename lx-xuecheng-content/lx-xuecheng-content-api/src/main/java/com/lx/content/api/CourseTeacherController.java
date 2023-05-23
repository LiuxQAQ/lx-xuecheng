package com.lx.content.api;

import com.lx.content.model.dto.SaveCourseTeacherDto;
import com.lx.content.model.dto.SaveTeachplanDto;
import com.lx.content.model.entity.CourseTeacher;
import com.lx.content.service.CourseTeacherService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Api(value = "老师管理接口",tags = "老师管理接口")
public class CourseTeacherController {

    @Autowired
    private CourseTeacherService courseTeacherService;

    @ApiOperation("教师查询接口")
    @GetMapping("/courseTeacher/list/{courseId}")
    public List<CourseTeacher> list(@PathVariable("courseId") Long courseId){
        return courseTeacherService.list(courseId);
    }

    @ApiOperation("保存教师信息")
    @PostMapping("/CourseTeacher")
    public SaveCourseTeacherDto saveCourseTeacher(@RequestBody SaveCourseTeacherDto teacherDto){
        Long companyId = 1232141425L;
        return courseTeacherService.saveCourseTeacher(companyId,teacherDto);
    }

    @DeleteMapping("/ourseTeacher/course/{courseId}/{teacherId}")
    public void deleteCourserTeacher(@PathVariable("courseId") Long courseId,@PathVariable("teacherId") Long teacherId){
        courseTeacherService.deleteCourseTeacher(courseId,teacherId);
    }

}
