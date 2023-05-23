package com.lx.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lx.base.execption.XueChengException;
import com.lx.base.model.PageParams;
import com.lx.base.model.PageResult;
import com.lx.content.mapper.CourseBaseMapper;
import com.lx.content.mapper.CourseCategoryMapper;
import com.lx.content.mapper.CourseMarketMapper;
import com.lx.content.model.dto.AddCourseDto;
import com.lx.content.model.dto.CourseBaseInfoDto;
import com.lx.content.model.dto.EditCourseDto;
import com.lx.content.model.dto.QueryCourseParamsDto;
import com.lx.content.model.entity.CourseBase;
import com.lx.content.model.entity.CourseCategory;
import com.lx.content.model.entity.CourseMarket;
import com.lx.content.service.CourseBaseInfoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;


@Service
public class CourseBaseInfoServiceImpl implements CourseBaseInfoService {

    public static final String COURSE_NOT_SUBMIT = "202002";
    public static final String COURSE_NOT_PUBLISH = "203001";
    public static final String COURSE_CHARGE = "201001";

    @Autowired
    private CourseBaseMapper courseBaseMapper;
    @Autowired
    private CourseMarketMapper courseMarketMapper;
    @Autowired
    private CourseCategoryMapper courseCategoryMapper;
    @Override
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParams) {
        // 构建查询条件对象
        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();

        // 构建查询条件,根据课程名称查询
        queryWrapper.like(StringUtils.isNotEmpty(queryCourseParams.getCourseName()),
                CourseBase::getName,queryCourseParams.getCourseName());

        // 构建查询条件,根据审核状态查询
        queryWrapper.eq(StringUtils.isNotEmpty(queryCourseParams.getAuditStatus()),
                CourseBase::getAuditStatus,queryCourseParams.getAuditStatus());

        // 根据课程发布状态查询
        queryWrapper.eq(StringUtils.isNotEmpty(queryCourseParams.getPublishStatus()),
                CourseBase::getStatus,queryCourseParams.getPublishStatus());

        // 分页对象
        Page<CourseBase> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 查询数据内容获得结果
        Page<CourseBase> pageResult = courseBaseMapper.selectPage(page, queryWrapper);
        // 获取数据列表
        List<CourseBase> list = pageResult.getRecords();
        // 获取数据总数
        long total = pageResult.getTotal();
        // 构建结果集
        PageResult<CourseBase> result = new PageResult<CourseBase>(list,total,pageParams.getPageNo(),pageParams.getPageSize());
        return result;
    }

    @Override
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto dto) {

        //  合法性校验
        if (StringUtils.isBlank(dto.getName())){
            throw new XueChengException("课程名称为空");
        }
        if (StringUtils.isBlank(dto.getMt())){
            throw new XueChengException("课程分类为空");
        }
        if (StringUtils.isBlank(dto.getSt())){
            throw new XueChengException("课程分类为空");
        }
        if (StringUtils.isBlank(dto.getGrade())){
            throw new XueChengException("课程等级为空");
        }
        if (StringUtils.isBlank(dto.getTeachmode())){
            throw new XueChengException("教育模式为空");
        }
        if (StringUtils.isBlank(dto.getUsers())){
            throw new XueChengException("适应人群为空");
        }
        if (StringUtils.isBlank(dto.getCharge())){
            throw new XueChengException("收费规则为空");
        }

        // 新增对象
        CourseBase courseBaseNew = new CourseBase();
        // 将填写的课程信息赋值给新增对象
        BeanUtils.copyProperties(dto,courseBaseNew);
        // 设置审核状态
        courseBaseNew.setAuditStatus(COURSE_NOT_SUBMIT);
        // 设置发布状态
        courseBaseNew.setStatus(COURSE_NOT_PUBLISH);
        // 机构id
        courseBaseNew.setCompanyId(companyId);
        // 添加时间
        courseBaseNew.setCreateDate(LocalDateTime.now());
        // 插入课程基本表
        int insert = courseBaseMapper.insert(courseBaseNew);
        if (insert <= 0){
            throw new XueChengException("新增课程基本信息失败");
        }

        // 向课程营销表保存课程营销信息
        // 课程营销信息
        CourseMarket courseMarketNew = new CourseMarket();
        Long courseId = courseBaseNew.getId();
        BeanUtils.copyProperties(dto,courseMarketNew);
        courseMarketNew.setId(courseId);
        int i = saveCourseMarket(courseMarketNew);
        if (i <= 0){
            throw new XueChengException("保存课程营销信息失败");
        }
        // 查询课程基本信息及营销信息并返回
        return getCourseBaseInfo(courseId);
    }

    // 保存课程营销信息
    public int saveCourseMarket(CourseMarket courseMarketNew){
        // 收费规则
        String charge = courseMarketNew.getCharge();
        // 合法性校验
        if (StringUtils.isBlank(charge)){
            throw new XueChengException("收费规则没选择");
        }
        // 收费规则为收费
        if (COURSE_CHARGE.equals(charge)){
            if (courseMarketNew.getPrice()==null || courseMarketNew.getPrice() <=0){
                throw new XueChengException("课程为收费价格不能为空且价格必须大于0");
            }
        }
        // 根据id从课程营销表中查询
        CourseMarket courseMarketObj = courseMarketMapper.selectById(courseMarketNew.getId());
        if (courseMarketObj==null){
            return courseMarketMapper.insert(courseMarketNew);
        }else {
            BeanUtils.copyProperties(courseMarketNew,courseMarketObj);
            courseMarketObj.setId(courseMarketNew.getId());
            return courseMarketMapper.updateById(courseMarketObj);
        }
    }

    // 根据课程id查询课程基本信息，包括基本信息和营销信息
    public CourseBaseInfoDto getCourseBaseInfo(long courseId){
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (courseBase == null){
            return null;
        }

        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(courseBase,courseBaseInfoDto);
        if (courseMarket != null){
            BeanUtils.copyProperties(courseMarket,courseBaseInfoDto);
        }

        // 查询分类名称
        CourseCategory courseCategoryBySt = courseCategoryMapper.selectById(courseBase.getSt());
        CourseCategory courseCategoryByMt = courseCategoryMapper.selectById(courseBase.getMt());
        courseBaseInfoDto.setStName(courseCategoryBySt.getName());
        courseBaseInfoDto.setMtName(courseCategoryByMt.getName());
        return courseBaseInfoDto;
    }

    @Transactional
    @Override
    public CourseBaseInfoDto updateCourseBase(Long companyId, EditCourseDto dto) {

        // 课程id
        Long courseId = dto.getId();
        CourseBase courseBase = courseBaseMapper.selectById(courseId);

        if (courseBase==null){
            XueChengException.cast("课程不存在");
        }

        // 校验本机构只能修改本机构的课程
        if (!courseBase.getCompanyId().equals(companyId)){
            XueChengException.cast("本机构无权修改其他机构的课程");
        }

        // 封装基本信息的数据
        BeanUtils.copyProperties(dto,courseBase);
        courseBase.setChangeDate(LocalDateTime.now());

        // 更新课程基本信息
        int i = courseBaseMapper.updateById(courseBase);

        // 封装营销信息数据
        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(dto,courseMarket);
        saveCourseMarket(courseMarket);
        // 查询课程信息
        CourseBaseInfoDto courseBaseInfo = this.getCourseBaseInfo(courseId);

        return courseBaseInfo;
    }

    @Override
    public void removeCourseBaseInfo(Long companyId, Long courseId) {
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (courseBase == null){
            XueChengException.cast("该课程不存在");
        }

        if (courseBase.getCompanyId().equals(companyId)){
            XueChengException.cast("您无权删除其他机构的课程");
        }

        courseBaseMapper.deleteById(courseBase.getId());
        courseMarketMapper.deleteById(courseBase.getId());
    }
}
