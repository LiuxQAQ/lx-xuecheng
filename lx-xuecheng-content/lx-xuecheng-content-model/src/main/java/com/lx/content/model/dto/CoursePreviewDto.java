package com.lx.content.model.dto;

import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @description 课程预览数据模型
 * @author lx
 * @date 2023/5/27
 */
 @Data
 @ToString
public class CoursePreviewDto {

    //课程基本信息,课程营销信息
    CourseBaseInfoDto courseBase;

    //课程计划信息
    List<TeachplanDto> teachplans;
    
    //师资信息暂时不加...


}