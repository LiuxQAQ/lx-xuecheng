package com.lx.search.service;

import com.lx.base.model.PageParams;
import com.lx.search.model.dto.SearchCourseParamDto;
import com.lx.search.model.dto.SearchPageResultDto;
import com.lx.search.model.entity.CourseIndex;

/**
 * @author lx
 * @date 2023/5/29 22:32
 * @description 课程搜索service
 */
public interface CourseSearchService {

    /**
     * @author lx
     * @date 2023/5/29 22:33
     * @param
     * @description 搜索课程列表
     */
    public SearchPageResultDto<CourseIndex> queryCoursePubIndex(PageParams pageParams, SearchCourseParamDto searchCourseParamDto);
}
