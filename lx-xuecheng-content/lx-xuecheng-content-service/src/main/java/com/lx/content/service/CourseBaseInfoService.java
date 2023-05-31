package com.lx.content.service;

import com.lx.base.model.PageParams;
import com.lx.base.model.PageResult;
import com.lx.content.model.dto.AddCourseDto;
import com.lx.content.model.dto.CourseBaseInfoDto;
import com.lx.content.model.dto.EditCourseDto;
import com.lx.content.model.dto.QueryCourseParamsDto;
import com.lx.content.model.entity.CourseBase;
import org.springframework.stereotype.Service;

public interface CourseBaseInfoService {
    /**
     * @author lx
     * @date 2023/5/17 20:10
     * @param  companyId 机构id
     * @param pageParams 分页参数
     * @param queryCourseParams 查询条件
     * @description 课程查询接口
     */
    public PageResult<CourseBase> queryCourseBaseList(Long companyId,PageParams pageParams, QueryCourseParamsDto queryCourseParams);

    /**
     * @author lx
     * @date 2023/5/18 22:32
     * @param companyId 教学机构id
     * @param addCourseDto 课程基本信息
     * @description 添加课程基本信息
     */
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto addCourseDto);

    /**
     * @author lx
     * @date 2023/5/21 19:42
     * @param courseId 课程id
     * @description 根据课程id查询课程基本信息
     */
    public CourseBaseInfoDto getCourseBaseInfo(long courseId);

    /**
     * @author lx
     * @date 2023/5/21 19:50
     * @param  companyId 机构id
     * @param  dto 课程信息
     * @description 修改课程信息
     
     */
    public CourseBaseInfoDto updateCourseBase(Long companyId, EditCourseDto dto);

    /**
     * @author lx
     * @date 2023/5/22 20:54
     * @param companyId 企业id
     * @param courseId 课程id
     * @description

     */
    public void removeCourseBaseInfo(Long companyId,Long courseId);
}
