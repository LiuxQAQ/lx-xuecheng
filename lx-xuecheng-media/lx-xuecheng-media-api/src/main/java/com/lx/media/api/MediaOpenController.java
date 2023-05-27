package com.lx.media.api;

import com.lx.base.execption.XueChengException;
import com.lx.base.model.RestResponse;
import com.lx.media.model.entity.MediaFiles;
import com.lx.media.service.MediaFilesService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.simpleframework.xml.Path;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(value = "媒资文件管理接口",tags = "媒资文件管理接口")
@RequestMapping("/open")
@RestController
public class MediaOpenController {

    @Autowired
    private MediaFilesService mediaFilesService;

    @ApiOperation("预览文件")
    @GetMapping("/preview/{mediaId}")
    public RestResponse<String> getPlayUrlByMediaId(@PathVariable("mediaId") String mediaId){
        MediaFiles mediaFiles = mediaFilesService.getFileById(mediaId);
        if (mediaFiles == null || StringUtils.isEmpty(mediaFiles.getUrl())){
            XueChengException.cast("视频还没有转码处理");
        }
        return RestResponse.success(mediaFiles.getUrl());
    }


}
