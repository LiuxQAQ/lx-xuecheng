package com.lx.content.service;

import com.lx.base.model.PageParams;
import com.lx.base.model.PageResult;
import com.lx.content.model.dto.AddCourseDto;
import com.lx.content.model.dto.CourseBaseInfoDto;
import com.lx.content.model.dto.QueryCourseParamsDto;
import com.lx.content.model.entity.CourseBase;
import org.springframework.stereotype.Service;

public interface CourseBaseInfoService {
    /**
     * @author lx
     * @date 2023/5/17 20:10
     * @param pageParams 分页参数
     * @param queryCourseParams 查询条件
     * @description 课程查询接口
     */
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParams);

    /**
     * @author lx
     * @date 2023/5/18 22:32
     * @param companyId 教学机构id
     * @param addCourseDto 课程基本信息
     * @description 添加课程基本信息
     */
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto addCourseDto);

}
