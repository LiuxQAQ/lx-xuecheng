package com.lx.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lx.system.model.dto.CourseCategoryTreeDto;
import com.lx.system.model.entity.CourseCategory;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 * 课程分类 Mapper 接口
 * </p>
 *
 * @author itcast
 */
@Repository
public interface CourseCategoryMapper extends BaseMapper<CourseCategory> {
    public List<CourseCategoryTreeDto> selectTreeNodes();
}
