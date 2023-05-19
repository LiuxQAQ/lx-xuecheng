package com.lx.content.service;

import com.lx.content.model.dto.CourseCategoryTreeDto;
import org.springframework.stereotype.Service;

import java.util.List;


public interface CourseCategoryService {
    /**
     * @author lx
     * @date 2023/5/18 20:41
     * @param id 根节点id
     * @description 课程分类树形结构查询
     */
    public List<CourseCategoryTreeDto> queryTreeNodes(String id);
}
