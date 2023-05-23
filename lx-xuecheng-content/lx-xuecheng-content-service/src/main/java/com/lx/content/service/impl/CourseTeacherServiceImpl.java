package com.lx.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lx.base.execption.XueChengException;
import com.lx.content.mapper.CourseTeacherMapper;
import com.lx.content.model.dto.SaveCourseTeacherDto;
import com.lx.content.model.entity.CourseTeacher;
import com.lx.content.service.CourseTeacherService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseTeacherServiceImpl implements CourseTeacherService {

    @Autowired
    private CourseTeacherMapper courseTeacherMapper;

    @Override
    public List<CourseTeacher> list(Long courseId) {
        List<CourseTeacher> courseTeachers = courseTeacherMapper.selectList(new LambdaQueryWrapper<CourseTeacher>().eq(CourseTeacher::getCourseId, courseId));
        if (courseTeachers==null){
            XueChengException.cast("该课程没有老师信息");
        }
        return courseTeachers;
    }

    @Override
    public SaveCourseTeacherDto saveCourseTeacher(Long company,SaveCourseTeacherDto saveCourseTeacherDto) {

        // 教师id
        Long id = saveCourseTeacherDto.getId();
        // 修改老师信息
        if (id != null){
            CourseTeacher courseTeacher = courseTeacherMapper.selectById(id);
            BeanUtils.copyProperties(saveCourseTeacherDto,courseTeacher);
            courseTeacherMapper.updateById(courseTeacher);
        }else {
            CourseTeacher courseTeacherNew = new CourseTeacher();
            BeanUtils.copyProperties(saveCourseTeacherDto,courseTeacherNew);
            courseTeacherMapper.insert(courseTeacherNew);
        }
        return saveCourseTeacherDto;
    }

    @Override
    public void deleteCourseTeacher(Long courseId, Long teacherId) {
        CourseTeacher courseTeacher = courseTeacherMapper.selectById(teacherId);
        if (courseTeacher == null){
            XueChengException.cast("没有该老师信息删除失败");
        }
        if (!courseTeacher.getCourseId().equals(courseId)){
            XueChengException.cast("该老师不在此课程中");
        }
        courseTeacherMapper.deleteById(courseTeacher.getId());
    }
}
