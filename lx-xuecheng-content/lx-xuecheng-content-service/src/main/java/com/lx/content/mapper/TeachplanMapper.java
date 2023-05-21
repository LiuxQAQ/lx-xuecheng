package com.lx.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lx.content.model.dto.TeachplanDto;
import com.lx.content.model.entity.Teachplan;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 * 课程计划 Mapper 接口
 * </p>
 *
 * @author itcast
 */
@Repository
public interface TeachplanMapper extends BaseMapper<Teachplan> {

    /**
     * @author lx
     * @date 2023/5/21 21:12
     * @param  courseId
     * @description 查询某课程的课程计划,组成树形结构
     */
    public List<TeachplanDto> selectTreeNodes(long courseId);
}
