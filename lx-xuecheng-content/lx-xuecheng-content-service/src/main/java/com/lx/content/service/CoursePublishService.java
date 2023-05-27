package com.lx.content.service;

import com.lx.content.model.dto.CoursePreviewDto;

/**
 * @author lx
 * @date 2023/5/27 16:26
 * @description 课程预览，发布接口
 */
public interface CoursePublishService {

    /**
     * @description 获取课程预览信息
     * @param courseId 课程id
     * @return CoursePreviewDto
     * @author lx
     * @date 2023/5/27
     */
    public CoursePreviewDto getCoursePreviewInfo(Long courseId);
}
