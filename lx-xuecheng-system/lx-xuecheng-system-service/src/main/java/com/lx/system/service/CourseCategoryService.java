package com.lx.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lx.system.model.dto.CourseCategoryTreeDto;
import com.lx.system.model.entity.CourseCategory;

import java.util.List;

/**
 * <p>
 * 课程分类 服务类
 * </p>
 *
 * @author itcast
 * @since 2023-05-17
 */
public interface CourseCategoryService extends IService<CourseCategory> {

    List<CourseCategoryTreeDto> queryTreeNodes();
}
