package com.lx.content.model.dto;

import com.lx.content.model.entity.CourseTeacher;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@ApiModel(value = "SaveCourseTeacherDto",description = "保存教师信息DTO，包括添加和修改")
public class SaveCourseTeacherDto extends CourseTeacher {
}
