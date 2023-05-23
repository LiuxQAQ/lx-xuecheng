package com.lx.content.service;

import com.lx.content.model.dto.SaveCourseTeacherDto;
import com.lx.content.model.dto.SaveTeachplanDto;
import com.lx.content.model.entity.CourseTeacher;

import java.util.List;

public interface CourseTeacherService {
    
    /**
     * @author lx
     * @date 2023/5/22 18:00
     * @param courseId 课程id
     * @description 查询教师接口
     */
    public List<CourseTeacher> list(Long courseId);

    /**
     * @author lx
     * @date 2023/5/22 19:10
     * @param saveCourseTeacherDto 教师信息
     * @param companyId 机构id
     * @description 添加或修改教师信息
     */
    public SaveCourseTeacherDto saveCourseTeacher(Long companyId,SaveCourseTeacherDto saveCourseTeacherDto);

    /**
     * @author lx
     * @date 2023/5/22 20:37
     * @param courseId 课程id
     * @param teacherId 教师id
     * @description 删除教师信息
     */
    public void deleteCourseTeacher(Long courseId,Long teacherId);
}
