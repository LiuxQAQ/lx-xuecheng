package com.lx.content.model.dto;

import com.lx.content.model.entity.Teachplan;
import com.lx.content.model.entity.TeachplanMedia;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class TeachplanDto extends Teachplan {

    // 课程计划关联的媒资信息
    TeachplanMedia teachplanMedia;

    // 子节点
    List<TeachplanDto> teachplanTreeNodes;

}
