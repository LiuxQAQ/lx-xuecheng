package com.lx.learning.service;

import com.lx.base.model.PageResult;
import com.lx.learning.model.dto.MyCourseTableParams;
import com.lx.learning.model.dto.XcChooseCourseDto;
import com.lx.learning.model.dto.XcCourseTablesDto;
import com.lx.learning.model.entity.XcCourseTables;

/**
 * @author lx
 * @date 2023/5/31 22:58
 * @description 我的课程表service接口
 */
public interface MyCourseTableService {

    /**
     * @author lx
     * @date 2023/5/31 22:59
     * @param userId 用户id
     * @param courseId 课程id
     * @description 添加选课
     */
    public XcChooseCourseDto addChooseCourse(String userId,Long courseId);

    /**
     * @description 判断学习资格
     * @param userId
     * @param courseId
     * @return XcCourseTablesDto 学习资格状态 [{"code":"702001","desc":"正常学习"},{"code":"702002","desc":"没有选课或选课后没有支付"},{"code":"702003","desc":"已过期需要申请续期或重新支付"}]
     * @author lx
     * @date 2023/6/1
     */
    public XcCourseTablesDto getLearningStatus(String userId, Long courseId);

    /**
     * 保存选课成功状态
     * @param chooseCourseId
     * @return
     */
    public boolean saveChooseCourseSuccess(String chooseCourseId);

    /**
     * @description 我的课程表
     * @param params
     */
    public PageResult<XcCourseTables> mycourestabls(MyCourseTableParams params);
}
