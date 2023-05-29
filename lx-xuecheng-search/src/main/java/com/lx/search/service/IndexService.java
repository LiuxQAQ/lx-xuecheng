package com.lx.search.service;

/**
 * @author lx
 * @date 2023/5/29 21:21
 * @description 课程索引service
 */
public interface IndexService {

    
    /**
     * @author lx
     * @date 2023/5/29 21:23
     * @param indexName 索引名称
     * @param id 主键
     * @param object 索引对象
     * @description 添加索引
     */
    public Boolean addCourseIndex(String indexName,String id,Object object);

}
