package com.lx.learning.service.impl;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.lx.base.execption.XueChengException;
import com.lx.base.model.RestResponse;
import com.lx.content.model.entity.CoursePublish;
import com.lx.learning.feignclient.ContentServiceClient;
import com.lx.learning.feignclient.MediaServiceClient;
import com.lx.learning.model.dto.XcCourseTablesDto;
import com.lx.learning.service.LearningService;
import com.lx.learning.service.MyCourseTableService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LearningServiceImpl implements LearningService {

    @Autowired
    private ContentServiceClient contentServiceClient;
    @Autowired
    private MediaServiceClient mediaServiceClient;
    @Autowired
    private MyCourseTableService myCourseTableService;

    @Override
    public RestResponse<String> getVideo(String userId, Long courseId, Long teachplanId, String mediaId) {

        // 查询课程信息
        CoursePublish coursePublish = contentServiceClient.getCoursePublish(courseId);
        if (coursePublish == null){
            XueChengException.cast("课程信息不存在");
        }
        // 校验学习资格

        //如果登录
        if(StringUtils.isNotEmpty(userId)){

            //判断是否选课，根据选课情况判断学习资格
            XcCourseTablesDto xcCourseTablesDto = myCourseTableService.getLearningStatus(userId, courseId);
            //学习资格状态 [{"code":"702001","desc":"正常学习"},{"code":"702002","desc":"没有选课或选课后没有支付"},{"code":"702003","desc":"已过期需要申请续期或重新支付"}]
            String learnStatus = xcCourseTablesDto.getLearnStatus();
            if(learnStatus.equals("702001")){
                return mediaServiceClient.getPlayUrlByMediaId(mediaId);
            }else if(learnStatus.equals("702003")){
                RestResponse.validfail("您的选课已过期需要申请续期或重新支付");
            }
        }

        //未登录或未选课判断是否收费
        String charge = coursePublish.getCharge();
        if(charge.equals("201000")){//免费可以正常学习
            return mediaServiceClient.getPlayUrlByMediaId(mediaId);
        }

        return RestResponse.validfail("请购买课程后继续学习");
    }
}
