package com.lx.content.service.impl;

import com.alibaba.fastjson.JSON;
import com.lx.base.execption.CommonError;
import com.lx.base.execption.XueChengException;
import com.lx.content.config.MultipartSupportConfig;
import com.lx.content.feignclient.MediaServiceClient;
import com.lx.content.mapper.CourseBaseMapper;
import com.lx.content.mapper.CourseMarketMapper;
import com.lx.content.mapper.CoursePublishMapper;
import com.lx.content.mapper.CoursePublishPreMapper;
import com.lx.content.model.dto.CourseBaseInfoDto;
import com.lx.content.model.dto.CoursePreviewDto;
import com.lx.content.model.dto.TeachplanDto;
import com.lx.content.model.entity.*;
import com.lx.content.service.CourseBaseInfoService;
import com.lx.content.service.CoursePublishService;
import com.lx.content.service.TeachplanService;
import com.lx.message.model.entity.MqMessage;
import com.lx.message.service.MqMessageService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class CoursePublishServiceImpl implements CoursePublishService {

    private static final String COURSE_SUBMITTED = "202003";
    private static final String AUDIT_PASS = "202004";
    private static final String PUBLISHED = "203002";

    @Autowired
    private CourseBaseInfoService courseBaseInfoService;
    @Autowired
    private TeachplanService teachplanService;
    @Autowired
    private CourseBaseMapper courseBaseMapper;
    @Autowired
    private CourseMarketMapper courseMarketMapper;
    @Autowired
    private CoursePublishPreMapper coursePublishPreMapper;
    @Autowired
    private CoursePublishMapper coursePublishMapper;
    @Autowired
    private MqMessageService mqMessageService;
    @Autowired
    private MediaServiceClient mediaServiceClient;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RedissonClient redissonClient;


    @Override
    public CoursePreviewDto getCoursePreviewInfo(Long courseId) {

        // 课程基本信息、营销信息
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(courseId);

        // 课程计划信息
        List<TeachplanDto> teachplanTree = teachplanService.findTeachplanTree(courseId);

        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();
        coursePreviewDto.setCourseBase(courseBaseInfo);
        coursePreviewDto.setTeachplans(teachplanTree);
        return coursePreviewDto;
    }

    @Override
    public void commitAudit(Long companyId, Long courseId) {
        // 约束校验
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        // 课程审核状态
        String auditStatus = courseBase.getAuditStatus();
        if (COURSE_SUBMITTED.equals(auditStatus)){
            XueChengException.cast("当前为等待审核状态，审核完成可以再次提交。");
        }

        // 本机构只允许提交本机构的课程
        if (!courseBase.getCompanyId().equals(companyId)){
            XueChengException.cast("不允许对其他机构的课程操作");
        }

        // 课程图片是否填写
        if (StringUtils.isEmpty(courseBase.getPic())){
            XueChengException.cast("提交失败，请上传课程图片");
        }

        // 添加课程预发布记录
        CoursePublishPre coursePublishPre = new CoursePublishPre();

        // 课程基本信息加部分营销信息
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(courseId);
        BeanUtils.copyProperties(courseBaseInfo,coursePublishPre);
        // 课程营销信息
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        // 转为json
        String courseMarketJson = JSON.toJSONString(courseMarket);
        // 将课程营销信息json数据放入课程预发布表
        coursePublishPre.setMarket(courseMarketJson);

        // 查询课程计划信息
        List<TeachplanDto> teachplanTree = teachplanService.findTeachplanTree(courseId);
        if (teachplanTree.size() <= 0){
            XueChengException.cast("提交失败,还没有添加课程计划");
        }
        // 转json
        String teachplanTreeJson = JSON.toJSONString(teachplanTree);
        coursePublishPre.setTeachplan(teachplanTreeJson);

        // 设置预发布记录状态，已提交
        coursePublishPre.setStatus(COURSE_SUBMITTED);
        // 教学机构id
        coursePublishPre.setCompanyId(companyId);
        // 提交时间
        coursePublishPre.setCreateDate(LocalDateTime.now());
        CoursePublishPre coursePublishPreUpdate = coursePublishPreMapper.selectById(courseId);
        if (coursePublishPreUpdate == null){
            // 添加课程预发布记录
            coursePublishPreMapper.insert(coursePublishPre);
        }else {
            coursePublishPreMapper.updateById(coursePublishPre);
        }

        // 更新课程基本表的审核状态
        courseBase.setAuditStatus(COURSE_SUBMITTED);
        courseBaseMapper.updateById(courseBase);
    }

    @Transactional
    @Override
    public void publish(Long companyId, Long courseId) {
        // 约束校验
        // 查询课程预发布表
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        if (coursePublishPre == null){
            XueChengException.cast("请先提交课程审核，审核通过才可以发布");
        }
        if (!companyId.equals(coursePublishPre.getCompanyId())){
            XueChengException.cast("不允许提交其他机构的课程");
        }

        // 课程审核状态
        String status = coursePublishPre.getStatus();
        // 审核通过方可发布
        if (!AUDIT_PASS.equals(status)){
            XueChengException.cast("操作失败，课程审核通过方可发布");
        }

        // 保存课程发布信息
        saveCoursePublish(courseId);
        // 保存消息表信息
        saveCoursePublishMessage(courseId);
        // 删除课程预发布表中的记录
        coursePublishPreMapper.deleteById(courseId);
    }

    /**
     * @author lx
     * @date 2023/5/28 15:01
     * @param courseId 课程id
     * @description 保存课程发布信息
     */
    private void saveCoursePublish(Long courseId){
        // 整合课程发布信息
        // 查询课程预发布表
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        if (coursePublishPre == null){
            XueChengException.cast("课程预发布数据为空");
        }

        CoursePublish coursePublish = new CoursePublish();
        // copy
        BeanUtils.copyProperties(coursePublishPre,coursePublish);
        coursePublish.setStatus(PUBLISHED);
        CoursePublish coursePublishUpdate = coursePublishMapper.selectById(courseId);
        if (coursePublishUpdate == null){
            coursePublishMapper.insert(coursePublish);
        }else {
            coursePublishMapper.updateById(coursePublish);
        }

        // 更新课程基本表的发布状态
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        courseBase.setStatus(PUBLISHED);
        courseBaseMapper.updateById(courseBase);
    }
    
    /**
     * @author lx
     * @date 2023/5/28 15:10
     * @param courseId 课程id
     * @description 保存消息表记录
     */
    private void saveCoursePublishMessage(Long courseId){
        MqMessage mqMessage = mqMessageService.addMessage("course_publish", String.valueOf(courseId), null, null);
        if (mqMessage == null){
            XueChengException.cast(CommonError.UNKNOWN_ERROR);
        }

    }

    @Override
    public File generateCourseHtml(Long courseId) {
        // 静态化文件
        File htmlFile = null;

        try {
            // 配置freemarker
            Configuration configuration = new Configuration(Configuration.getVersion());

            // 加载模板
            // 选指定模板路径,classpath下templates下
            String classPath = this.getClass().getResource("/").getPath();
            configuration.setDirectoryForTemplateLoading(new File(classPath+"/templates/"));
            // 设置字符编码
            configuration.setDefaultEncoding("utf-8");

            // 指定模板文件名称
            Template template = configuration.getTemplate("course_template.ftl");

            // 准备数据
            CoursePreviewDto coursePreviewInfo = this.getCoursePreviewInfo(courseId);

            Map<String,Object> map = new HashMap<>();
            map.put("model",coursePreviewInfo);

            // 静态化
            // 参数1 模板 ；参数2 数据类型
            String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);
            // 将静态化内容输出到文件中
            InputStream inputStream = IOUtils.toInputStream(content);
            // 创建静态化文件
            htmlFile = File.createTempFile("course", ".html");
            log.debug("课程静态化，生成静态文件:{}",htmlFile.getAbsolutePath());
            // 输出流
            FileOutputStream outputStream = new FileOutputStream(htmlFile);
            IOUtils.copy(inputStream,outputStream);
        } catch (Exception e) {
            log.error("课程静态化异常:{}",e.toString());
            XueChengException.cast("课程静态化异常");
        }
        return htmlFile;
    }

    @Override
    public void uploadCourseHtml(Long courseId, File file) {
        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(file);
        String course = mediaServiceClient.uploadFile(multipartFile, "course/" + courseId + ".html");
        if (course == null){
            XueChengException.cast("上传静态文件异常");
        }
    }

    @Override
    public CoursePublish getCoursePublish(Long courseId) {
        return coursePublishMapper.selectById(courseId);
    }

    //解决缓存穿透
//    @Override
//    public CoursePublish getCoursePublishCache(Long courseId) {
//        //从缓存中查询
//        Object jsonObj = redisTemplate.opsForValue().get("course:" + courseId);
//        //缓存中有
//        if(jsonObj!=null){
////            System.out.println("=============从缓存中查询=============");
//            //缓存中有直接返回数据
//            String jsonString = jsonObj.toString();
//            if("null".equals(jsonString)){
//                return null;
//            }
//            CoursePublish coursePublish = JSON.parseObject(jsonString, CoursePublish.class);
//            return coursePublish;
//        }else{
//            System.out.println("==查询数据库==");
//            //从数据库查询
//            CoursePublish coursePublish = getCoursePublish(courseId);
////            if(coursePublish!=null){
//                //查询完成再存储到redis
//                redisTemplate.opsForValue().set("course:"+courseId,JSON.toJSONString(coursePublish),30, TimeUnit.SECONDS);
////            }
//            return coursePublish;
//        }
//    }

    //使用同步锁解决缓存击穿
//    @Override
//    public CoursePublish getCoursePublishCache(Long courseId) {
//
//
//            //从缓存中查询
//            Object jsonObj = redisTemplate.opsForValue().get("course:" + courseId);
//            //缓存中有
//            if(jsonObj!=null){
////            System.out.println("=============从缓存中查询=============");
//                //缓存中有直接返回数据
//                String jsonString = jsonObj.toString();
//                if("null".equals(jsonString)){
//                    return null;
//                }
//                CoursePublish coursePublish = JSON.parseObject(jsonString, CoursePublish.class);
//                return coursePublish;
//            }else{
//                synchronized (this){
//                    //再次查询一下缓存
//                    //从缓存中查询
//                    jsonObj = redisTemplate.opsForValue().get("course:" + courseId);
//                    //缓存中有
//                    if(jsonObj!=null) {
//                        //缓存中有直接返回数据
//                        String jsonString = jsonObj.toString();
//                        if("null".equals(jsonString)){
//                            return null;
//                        }
//                        CoursePublish coursePublish = JSON.parseObject(jsonString, CoursePublish.class);
//                        return coursePublish;
//                    }
//                    System.out.println("==查询数据库==");
//                    //从数据库查询
//                    CoursePublish coursePublish = getCoursePublish(courseId);
//                    //查询完成再存储到redis
//                    redisTemplate.opsForValue().set("course:"+courseId,JSON.toJSONString(coursePublish),300, TimeUnit.SECONDS);
//                    return coursePublish;
//
//                }
//            }



    //使用redisson实现分布式锁
    @Override
    public CoursePublish getCoursePublishCache(Long courseId) {
        //从缓存中查询
        Object jsonObj = redisTemplate.opsForValue().get("course:" + courseId);
        //缓存中有
        if (jsonObj != null) {
//            System.out.println("=============从缓存中查询=============");
            //缓存中有直接返回数据
            String jsonString = jsonObj.toString();
            if ("null".equals(jsonString)) {
                return null;
            }
            CoursePublish coursePublish = JSON.parseObject(jsonString, CoursePublish.class);
            return coursePublish;
        } else {
            RLock lock = redissonClient.getLock("coursequerylock:" + courseId);
            //获取分布式锁
            lock.lock();
            try {
                //再次查询一下缓存
                //从缓存中查询
                jsonObj = redisTemplate.opsForValue().get("course:" + courseId);
                //缓存中有
                if (jsonObj != null) {
                    //缓存中有直接返回数据
                    String jsonString = jsonObj.toString();
                    if ("null".equals(jsonString)) {
                        return null;
                    }
                    CoursePublish coursePublish = JSON.parseObject(jsonString, CoursePublish.class);
                    return coursePublish;
                }
                System.out.println("==查询数据库==");
//                try {
//                    //手动延迟，测试锁的续期功能
//                    Thread.sleep(60000);
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
//                }
                //从数据库查询
                CoursePublish coursePublish = getCoursePublish(courseId);
                //查询完成再存储到redis
                redisTemplate.opsForValue().set("course:" + courseId, JSON.toJSONString(coursePublish), 300, TimeUnit.SECONDS);
                return coursePublish;

            } finally {
                //手动释放锁
                lock.unlock();
            }
        }
    }
}
