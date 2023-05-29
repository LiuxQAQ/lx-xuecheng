package com.lx.media.api;

import com.lx.media.model.dto.UploadFileParamsDto;
import com.lx.media.model.dto.UploadFileResultDto;
import com.lx.media.service.MediaFilesService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Api(value = "媒资文件管理接口",tags = "媒资文件管理接口")
@RestController
public class MediaFilesController {

    public static final String IMAGE_TYPE = "001001";


    @Autowired
    private MediaFilesService mediaFilesService;


    @ApiOperation("上传图片")
    @RequestMapping(value = "/upload/coursefile",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadFileResultDto upload(@RequestPart("filedata")MultipartFile filedata,
                                      @RequestParam(value= "objectName",required=false) String objectName) throws IOException {

        //准备上传文件的信息
        UploadFileParamsDto uploadFileParamsDto = new UploadFileParamsDto();
        //原始文件名称
        uploadFileParamsDto.setFilename(filedata.getOriginalFilename());
        //文件大小
        uploadFileParamsDto.setFileSize(filedata.getSize());
        //文件类型
        uploadFileParamsDto.setFileType("001001");
        //创建一个临时文件
        File tempFile = File.createTempFile("minio", ".temp");
        filedata.transferTo(tempFile);
        Long companyId = 1232141425L;
        //文件路径
        String localFilePath = tempFile.getAbsolutePath();

        //调用service上传图片
        UploadFileResultDto uploadFileResultDto = mediaFilesService.uploadFile(companyId, uploadFileParamsDto, localFilePath,objectName);

        return uploadFileResultDto;
    }

}
