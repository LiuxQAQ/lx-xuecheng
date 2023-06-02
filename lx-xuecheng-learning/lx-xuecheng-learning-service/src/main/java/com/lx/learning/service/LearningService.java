package com.lx.learning.service;


import com.lx.base.model.RestResponse;

/**
 * @author lx
 * @date 2023/6/1 22:27
 * @description 学习过程管理接口

 */
public interface LearningService {

    /**
     * @description 获取教学视频
     * @param courseId 课程id
     * @param teachplanId 课程计划id
     * @param mediaId 视频文件id
     */
    public RestResponse<String> getVideo(String userId, Long courseId, Long teachplanId, String mediaId);
}
