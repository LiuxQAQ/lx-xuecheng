package com.lx.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.lx.base.execption.XueChengException;
import com.lx.search.service.IndexService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class IndexServiceImpl implements IndexService {

    @Qualifier("restHighLevelClient")
    @Autowired
    private RestHighLevelClient client;

    @Override
    public Boolean addCourseIndex(String indexName, String id, Object object) {
        String jsonString = JSON.toJSONString(object);
        IndexRequest indexRequest = new IndexRequest(indexName).id(id);
        // 指定索引文档内容
        indexRequest.source(jsonString, XContentType.JSON);
        // 索引响应对象
        IndexResponse indexResponse = null;
        try {
            indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);

        }catch (Exception e){
            log.error("添加索引出错:{}",e.getMessage());
            XueChengException.cast("添加索引出错");
        }
        String name = indexResponse.getResult().name();
        // equalsIgnoreCase() 用于字符串比较 忽略大小写
        return name.equalsIgnoreCase("created") ||
                name.equalsIgnoreCase("updated");
    }
}
