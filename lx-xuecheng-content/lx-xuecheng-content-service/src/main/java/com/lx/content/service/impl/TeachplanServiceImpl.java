package com.lx.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lx.base.execption.XueChengException;
import com.lx.content.mapper.CourseBaseMapper;
import com.lx.content.mapper.TeachplanMapper;
import com.lx.content.mapper.TeachplanMediaMapper;
import com.lx.content.model.dto.BindTeachplanMediaDto;
import com.lx.content.model.dto.SaveTeachplanDto;
import com.lx.content.model.dto.TeachplanDto;
import com.lx.content.model.entity.CourseBase;
import com.lx.content.model.entity.Teachplan;
import com.lx.content.model.entity.TeachplanMedia;
import com.lx.content.service.TeachplanService;
import com.sun.org.apache.xml.internal.security.encryption.AgreementMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * @author lx
 * @date 2023/5/21 22:11
 * @description 课程计划service接口实现类
 */
@Service
@Slf4j
public class TeachplanServiceImpl implements TeachplanService {

    public static final String COURSE_NOT_SUBMIT = "202002";
    public static final String MOVE_UP = "moveup";


    @Autowired
    private TeachplanMapper teachplanMapper;
    @Autowired
    private CourseBaseMapper courseBaseMapper;
    @Autowired
    private TeachplanMediaMapper teachplanMediaMapper;

    @Override
    public List<TeachplanDto> findTeachplanTree(long courseId) {
        return teachplanMapper.selectTreeNodes(courseId);
    }

    @Override
    @Transactional
    public void saveTeachplan(SaveTeachplanDto teachplanDto) {
        // 课程计划id
        Long id = teachplanDto.getId();
        // 修改课程计划
        if (id != null){
            Teachplan teachplan = teachplanMapper.selectById(id);
            BeanUtils.copyProperties(teachplanDto,teachplan);
            teachplanMapper.updateById(teachplan);
        }else {
            // 取出同父同级别的课程计划数量
            int count = getTeachplanCount(teachplanDto.getCourseId(),teachplanDto.getParentid());
            Teachplan teachplanNew = new Teachplan();
            // 设置排序号
            teachplanNew.setOrderby(count+1);
            BeanUtils.copyProperties(teachplanDto,teachplanNew);

            teachplanMapper.insert(teachplanNew);
        }
    }

    /**
     * @author lx
     * @date 2023/5/22 9:13
     * @param courseId 课程id
     * @param parentId 父课程计划id
     * @description 获取最新的排序号
     */
    private int getTeachplanCount(long courseId,long parentId){
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getCourseId,courseId);
        queryWrapper.eq(Teachplan::getParentid,parentId);
        Integer count = teachplanMapper.selectCount(queryWrapper);
        return count;
    }

    @Transactional
    @Override
    public void removeTeachplan(Long teachplanId) {
        // 取出课程计划
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);
        if (teachplan==null){
            XueChengException.cast("课程计划不存在");
        }

        // 课程id
        Long courseId = teachplan.getCourseId();

        // 课程信息
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        // 审核状态
        String auditStatus = courseBase.getAuditStatus();

        // 只有当课程是未提交时方可删除
        if (!COURSE_NOT_SUBMIT.equals(auditStatus)){
            XueChengException.cast("删除失败，课程审核状态是未提交时方可删除");
        }

        // 如果课程计划为一级分类，下边没有子课程时方可删除
        Integer grade = teachplan.getGrade();
        if (grade == 1){
            Integer count = teachplanMapper.selectCount(new LambdaQueryWrapper<Teachplan>().eq(Teachplan::getParentid,teachplan.getId()));
            if (count > 0){
                XueChengException.cast("删除失败，课程计划下有子课程");
            }
        }

        //删除课程计划
        teachplanMapper.deleteById(teachplanId);
        // 删除课程计划相关联的媒资信息
        teachplanMediaMapper.delete(new LambdaQueryWrapper<TeachplanMedia>().eq(TeachplanMedia::getId,teachplan.getId()));
    }

    @Override
    public void moveTeachPlan(Long teachplanId, String moveType) {

        // 课程计划
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);

        // 查询同级别的课程计划
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getCourseId,teachplan.getCourseId())
                .eq(Teachplan::getParentid,teachplan.getParentid());

        List<Teachplan> teachplanList = teachplanMapper.selectList(queryWrapper);

        // 如果同级别只有一个课程计划，什么都不要处理
        if (teachplanList.size() <= 1) return;

        // 根据移动类型进行排序
        if (MOVE_UP.equals(moveType)){
            // 上移，找到比当前计划小的，交换位置
            // 降序，先找到当前计划，下一个就是要和他交换位置的计划
            Collections.sort(teachplanList, (o1, o2) -> o2.getOrderby() - o1.getOrderby());
        }else {
            // 升序
            Collections.sort(teachplanList, (o1, o2) -> o1.getOrderby() - o2.getOrderby());
        }

        // 找到当前计划
        Teachplan one = null;
        Teachplan two = null;
        Iterator<Teachplan> iterator = teachplanList.iterator();
        while (iterator.hasNext()){
            Teachplan next = iterator.next();
            if (next.getId().equals(teachplan.getId())){
                one = next;
                try {
                    two = iterator.next();
                }catch (Exception e){
                }
            }
        }
        swapTeachplan(one,two);
    }

    private void swapTeachplan(Teachplan left,Teachplan right){
        if (left == null || right==null){
            return;
        }

        Integer orderby_left = left.getOrderby();
        Integer orderby_right = right.getOrderby();
        left.setOrderby(orderby_right);
        right.setOrderby(orderby_left);
        teachplanMapper.updateById(left);
        teachplanMapper.updateById(right);
        log.debug("课程计划交换位置,left:{},right:{}",left.getId(),right.getId());
    }

    @Override
    @Transactional
    public TeachplanMedia associationMedia(BindTeachplanMediaDto bindTeachplanMediaDto) {
        // 教学计划id
        Long teachplanId = bindTeachplanMediaDto.getTeachplanId();
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);
        if (teachplan == null){
            XueChengException.cast("教学计划不存在");
        }
        Integer grade = teachplan.getGrade();
        if (grade != 2){
            XueChengException.cast("只允许第二级计划绑定媒资文件");
        }
        // 课程id
        Long courseId = teachplan.getCourseId();

        // 先删除原来该教学计划绑定的媒资
        teachplanMediaMapper.delete(new LambdaQueryWrapper<TeachplanMedia>().eq(TeachplanMedia::getTeachplanId,teachplanId));

        // 再添加教学计划与媒资的绑定关系
        TeachplanMedia teachplanMedia = new TeachplanMedia();
        teachplanMedia.setCourseId(courseId);
        teachplanMedia.setTeachplanId(teachplanId);
        teachplanMedia.setMediaFileName(bindTeachplanMediaDto.getFileName());
        teachplanMedia.setMediaId(bindTeachplanMediaDto.getMediaId());
        teachplanMedia.setCreateDate(LocalDateTime.now());
        teachplanMediaMapper.insert(teachplanMedia);
        return teachplanMedia;
    }

    @Override
    public void removeAssociationMedia(Long teachplanId, String mediaId) {
        LambdaQueryWrapper<TeachplanMedia> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TeachplanMedia::getTeachplanId,teachplanId)
                .eq(TeachplanMedia::getMediaId,mediaId);
        TeachplanMedia teachplanMedia = teachplanMediaMapper.selectOne(queryWrapper);
        if (teachplanMedia==null){
            XueChengException.cast("该课程没有绑定该媒资");
        }

        int i = teachplanMediaMapper.deleteById(teachplanMedia);
        if (i<=0){
            XueChengException.cast("解除绑定失败,请重试");
        }
    }
}
