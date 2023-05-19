package com.lx.content.service.impl;

import com.lx.content.mapper.CourseCategoryMapper;
import com.lx.content.model.dto.CourseCategoryTreeDto;
import com.lx.content.service.CourseCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CourseCategoryServiceImpl implements CourseCategoryService {

    @Autowired
    private CourseCategoryMapper courseCategoryMapper;

    @Override
    public List<CourseCategoryTreeDto> queryTreeNodes(String id) {
        // 获取结果集 还没有构成树
        List<CourseCategoryTreeDto> courseCategoryTreeDtos = courseCategoryMapper.selectTreeNodes(id);

        // 将list转为map，以备使用，排除根节点
        Map<String, CourseCategoryTreeDto> mapTemp = courseCategoryTreeDtos.stream()
                .filter(item -> !id.equals(item.getId()))
                // 第三个参数(key1, key2) -> key2中，如果key1与key2的key值相同，选择key2作为那个key所对应的value值
                .collect(Collectors.toMap(key -> key.getId(), value -> value, (key1, key2) -> key2));

        // 最终返回list
        List<CourseCategoryTreeDto> categoryTreeDtos = new ArrayList<>();
        // 以此遍历每个元素，排除根节点
        courseCategoryTreeDtos.stream()
                .filter(item-> !id.equals(item.getId()))
                .forEach(item->{
                    if (id.equals(item.getParentid())){
                        categoryTreeDtos.add(item);
                    }
                    // 找到当前节点的父节点
                    CourseCategoryTreeDto courseCategoryTreeDto =
                            mapTemp.get(item.getParentid());
                    if (courseCategoryTreeDto != null){
                        if (courseCategoryTreeDto.getChildrenTreeNodes() == null){
                            courseCategoryTreeDto.setChildrenTreeNodes(new ArrayList<CourseCategoryTreeDto>());
                        }
                        courseCategoryTreeDto.getChildrenTreeNodes().add(item);
                    }
                });


        return categoryTreeDtos;
    }
}
