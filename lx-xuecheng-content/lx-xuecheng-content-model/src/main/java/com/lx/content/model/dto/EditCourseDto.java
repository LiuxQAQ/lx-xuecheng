package com.lx.content.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.Data;

/**
 * @author lx
 * @date 2023/5/19 23:07
 * @description 编辑课程Dto
 */
@Data
@ApiModel(value = "EditCourseDto",description = "修改课程基本信息")
public class EditCourseDto extends AddCourseDto{

    // required 表示参数必传
    @ApiModelProperty(value = "课程id",required = true)
    private Long id;

}
