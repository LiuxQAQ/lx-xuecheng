package com.lx.content.service;

import com.lx.content.model.dto.CoursePreviewDto;
import com.lx.content.model.entity.CoursePublish;

import java.io.File;

/**
 * @author lx
 * @date 2023/5/27 16:26
 * @description 课程预览，发布接口
 */
public interface CoursePublishService {

    /**
     * @description 获取课程预览信息
     * @param courseId 课程id
     * @return CoursePreviewDto
     * @author lx
     * @date 2023/5/27
     */
    public CoursePreviewDto getCoursePreviewInfo(Long courseId);

    /**
     * @author lx
     * @date 2023/5/28 9:32
     * @param companyId 机构id
     * @param courseId 课程id
     * @description
     */
    public void commitAudit(Long companyId,Long courseId);

    /**
     * @author lx
     * @date 2023/5/28 14:47
     * @param companyId 机构id
     * @param courseId 课程id
     * @description 课程发布接口
     */
    public void publish(Long companyId,Long courseId);

    /**
     * @author lx
     * @date 2023/5/29 10:45
     * @param courseId 课程id
     * @description 课程静态化
     */
    public File generateCourseHtml(Long courseId);

    /**
     * @author lx
     * @date 2023/5/29 10:46
     * @param courseId 课程id
     * @param file 静态化文件
     * @description 上传课程静态化页面
     
     */
    public void uploadCourseHtml(Long courseId,File file);

    /**
     * @author lx
     * @date 2023/5/31 22:39
     * @param courseId 课程id
     * @description 查询课程发布信息
     */
    public CoursePublish getCoursePublish(Long courseId);

}
