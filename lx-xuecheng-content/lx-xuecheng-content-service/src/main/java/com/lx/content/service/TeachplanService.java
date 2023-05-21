package com.lx.content.service;

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
}
