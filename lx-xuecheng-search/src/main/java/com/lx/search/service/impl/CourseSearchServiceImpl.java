package com.lx.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.lx.base.model.PageParams;
import com.lx.search.model.dto.SearchCourseParamDto;
import com.lx.search.model.dto.SearchPageResultDto;
import com.lx.search.model.entity.CourseIndex;
import com.lx.search.service.CourseSearchService;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class CourseSearchServiceImpl implements CourseSearchService {

    @Value("${elasticsearch.course.index}")
    private String courseIndexStore;
    @Value("${elasticsearch.course.source_fields}")
    private String sourceFields;

    @Autowired
    private RestHighLevelClient client;


    @Override
    public SearchPageResultDto<CourseIndex> queryCoursePubIndex(PageParams pageParams, SearchCourseParamDto searchCourseParamDto) {

        // 设置索引
        SearchRequest searchRequest = new SearchRequest(courseIndexStore);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        // source 源字段过滤
        String[] sourceFieldsArray = sourceFields.split(",");
        searchSourceBuilder.fetchSource(sourceFieldsArray,new String[]{});

        // 分页
        Long pageNo = pageParams.getPageNo();
        Long pageSize = pageParams.getPageSize();
        int start = (int) ((pageNo - 1 ) * pageSize);
        searchSourceBuilder.from(start);
        searchSourceBuilder.size(Math.toIntExact(pageSize));
        // 布尔查询
        searchSourceBuilder.query(boolQueryBuilder);

        // 请求搜索
        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = null;
        try {
            searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        }catch (Exception e){
            log.error("课程搜索异常:{}",e.getMessage());
            return new SearchPageResultDto<CourseIndex>(new ArrayList(),0,0,0);
        }

        // 结果集处理
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        // 记录总数
        TotalHits totalHits = hits.getTotalHits();
        // 数据列表
        List<CourseIndex> list = new ArrayList<>();

        for (SearchHit hit : searchHits) {
            String sourceAsString = hit.getSourceAsString();
            CourseIndex courseIndex = JSON.parseObject(sourceAsString, CourseIndex.class);
            list.add(courseIndex);
        }

        SearchPageResultDto<CourseIndex> pageResult = new SearchPageResultDto<>(list, totalHits.value,pageNo,pageSize);
        return pageResult;
    }
}
