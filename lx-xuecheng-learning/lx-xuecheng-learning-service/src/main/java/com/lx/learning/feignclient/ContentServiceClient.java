package com.lx.learning.feignclient;

import com.lx.content.model.entity.CoursePublish;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author lx
 * @date 2023/5/31 23:03
 * @description 内容管理远程接口
 */
@RequestMapping("/content")
public interface ContentServiceClient {

    @ResponseBody
    @GetMapping("/r/coursepublish/{courseId}")
    public CoursePublish getCoursePublish(@PathVariable("courseId") Long courseId);
}
