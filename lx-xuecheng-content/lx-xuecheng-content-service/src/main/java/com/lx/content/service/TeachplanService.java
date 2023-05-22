package com.lx.content.service;

import com.lx.content.model.dto.SaveTeachplanDto;
import com.lx.content.model.dto.TeachplanDto;
import com.lx.content.model.entity.Teachplan;
import jdk.internal.dynalink.linker.LinkerServices;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author lx
 * @date 2023/5/21 22:08
 * @description 课程基本信息管理业务接口

 */
public interface TeachplanService {

    /**
     * @author lx
     * @date 2023/5/21 22:09
     * @param courseId
     * @description 查询课程计划树型结构
     */
    public List<TeachplanDto> findTeachplanTree(long courseId);

    /**
     * @author lx
     * @date 2023/5/22 9:00
     * @param dto 课程计划信息
     * @description 新增或修改课程计划
     */
    public void saveTeachplan(SaveTeachplanDto dto);

    /**
     * @author lx
     * @date 2023/5/22 14:48
     * @param teachplanId 课程计划id
     * @description 删除课程计划

     */
    public void removeTeachplan(Long teachplanId);

    /**
     * @author lx
     * @date 2023/5/22 15:30
     * @param teachplanId 课程计划id
     * @param moveType 移动方向
     * @description 移动课程计划

     */
    public void moveTeachPlan(Long teachplanId,String moveType);
}
