package com.lx.content.service.jobhandler;

import com.lx.content.service.CoursePublishService;
import com.lx.message.model.entity.MqMessage;
import com.lx.message.service.MessageProcessAbstract;
import com.lx.message.service.MqMessageService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class CoursePublishTask extends MessageProcessAbstract {

    @Autowired
    private CoursePublishService coursePublishService;

    @XxlJob("CoursePublishJobHandler")
    public void coursePublishJobHandler() throws Exception{
        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        log.debug("shardIndex = "+shardIndex+"shardTotal = "+shardTotal);
        // 参数： 分片序号、分片总数、消息类型、一次最多取到的任务数量、一次任务调度执行的超时时间
        process(shardIndex,shardTotal,"course_publish",30,60);
    }

    @Override
    public boolean execute(MqMessage mqMessage) {
        // 获取消息相关的业务
        String businessKey1 = mqMessage.getBusinessKey1();
        long courseId = Integer.parseInt(businessKey1);
        // 课程静态化
        generateCourseHtml(mqMessage,courseId);
        // 课程索引
        saveCourseIndex(mqMessage,courseId);
        // 课程缓存
        saveCourseCache(mqMessage,courseId);
        return false;
    }

    // 生成课程静态化页面并上传至文件系统
    public void generateCourseHtml(MqMessage mqMessage,long courseId){
        log.debug("开始进行课程静态化,课程id:{}",courseId);
        // 消息id
        Long id = mqMessage.getId();
        // 消息处理的server
        MqMessageService mqMessageService = this.getMqMessageService();
        // 消息幂等性处理
        int stageOne = mqMessageService.getStageOne(id);
        if (stageOne > 0){
            log.debug("课程静态化已经处理");
            return;
        }

        // 生成静态化页面
        File file = coursePublishService.generateCourseHtml(courseId);
        // 上传静态化页面
        if (file != null){
            coursePublishService.uploadCourseHtml(courseId,file);
        }
        // 保存第一阶段状态
        mqMessageService.completedStageOne(id);
    }

    // 将课程消息缓存至redis
    public void saveCourseCache(MqMessage mqMessage,Long courseId){
        log.debug("将课程信息缓存至redis,课程id:{}",courseId);
        try {
            TimeUnit.SECONDS.sleep(2);
        }catch (InterruptedException e){
            throw new RuntimeException(e);
        }
    }

    // 保存课程索引信息
    public void saveCourseIndex(MqMessage mqMessage,Long courseId){
        log.debug("保存课程索引信息,课程id:{}",courseId);
        try {
            TimeUnit.SECONDS.sleep(2);
        }catch (InterruptedException e){
            throw new RuntimeException(e);
        }
    }

}
