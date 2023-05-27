package com.lx.content.api;

import com.lx.content.model.dto.BindTeachplanMediaDto;
import com.lx.content.model.dto.SaveTeachplanDto;
import com.lx.content.model.dto.TeachplanDto;
import com.lx.content.model.entity.Teachplan;
import com.lx.content.service.TeachplanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author lx
 * @date 2023/5/21 21:03
 * @description 课程计划编辑接口
 */
@Api(value = "课程计划编辑接口",tags = "课程计划编辑接口")
@RestController
public class TeachplanController {

    @Autowired
    private TeachplanService teachplanService;

    @ApiOperation("查询课程计划树形结构")
    @ApiImplicitParam(value = "courseId",name = "课程id",required = true,dataType = "Long",paramType = "path")
    @GetMapping("/teachplan/{courseId}/tree-nodes")
    public List<TeachplanDto> getTreeNodes(@PathVariable Long courseId){
        return teachplanService.findTeachplanTree(courseId);
    }

    @ApiOperation("课程计划创建或修改")
    @PostMapping("/teachplan")
    public void saveTeachplan(@RequestBody SaveTeachplanDto teachplanDto){
        teachplanService.saveTeachplan(teachplanDto);
    }

    @ApiOperation(value = "删除课程计划")
    @DeleteMapping("/teachplan/{teachplanId}")
    public void removeTeachPlan(@PathVariable("teachplanId") Long teachplanId){
        teachplanService.removeTeachplan(teachplanId);
    }

    @ApiOperation(value = "移动课程计划")
    @PostMapping("teachplan/{moveType}/{teachplanId}")
    public void moveTeachPlan(@PathVariable("moveType") String moveType,@PathVariable("teachplanId") Long teachplanId){
        teachplanService.moveTeachPlan(teachplanId,moveType);
    }

    @ApiOperation(value = "课程计划和媒资信息绑定")
    @PostMapping("/teachplan/association/media")
    public void associationMedia(@RequestBody BindTeachplanMediaDto bindTeachplanMediaDto){
        teachplanService.associationMedia(bindTeachplanMediaDto);
    }

    @DeleteMapping("/association/media/{teachplanId}/{mediaId}")
    public void removeAssociationMedia(@PathVariable("teachplanId") Long teachplanId,@PathVariable("mediaId") String mediaId){

    }
}
