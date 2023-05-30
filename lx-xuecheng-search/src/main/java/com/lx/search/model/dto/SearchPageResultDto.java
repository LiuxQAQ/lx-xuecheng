package com.lx.search.model.dto;

import com.lx.base.model.PageParams;
import com.lx.base.model.PageResult;

import java.util.List;


/**
 * @author lx
 * @date 2023/5/29 22:04
 * @description 搜索结果类，继承PageResult，方便后期拓展
 
 */
public class SearchPageResultDto<T> extends PageResult {

    // 大分类列表
    private List<String> mtList;
    // 小分类列表
    private List<String> stList;


    public SearchPageResultDto(List<T> items, long counts, long page, long pageSize) {
        super(items, counts, page, pageSize);
    }
    
}
