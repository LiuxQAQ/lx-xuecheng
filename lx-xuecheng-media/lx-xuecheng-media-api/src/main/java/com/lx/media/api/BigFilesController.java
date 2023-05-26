package com.lx.media.api;

import com.lx.base.model.RestResponse;
import com.lx.media.model.dto.UploadFileParamsDto;
import com.lx.media.service.BigFilesService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * @author lx
 * @date 2023/5/24 10:20
 * @description 大文件上传接口
 */
@Api(value = "大文件上传接口",tags = "大文件上传接口")
@RestController
public class BigFilesController {

    @Autowired
    private BigFilesService bigFilesService;

    @ApiOperation(value = "上传文件前检查文件")
    @PostMapping("/upload/checkfile")
    public RestResponse<Boolean> checkFile(@RequestParam("fileMd5") String fileMd5) throws Exception{
        return bigFilesService.checkFile(fileMd5);
    }

    @ApiOperation(value = "分块文件上传前的检测")
    @PostMapping("/upload/checkchunk")
    public RestResponse<Boolean> checkChunk(@RequestParam("fileMd5") String fileMd5,
                                            @RequestParam("chunk") int chunk) throws Exception{
        return bigFilesService.checkChunk(fileMd5, chunk);
    }

    @ApiOperation(value = "上传分块文件")
    @PostMapping("/upload/uploadchunk")
    public RestResponse uploadChunk(@RequestParam("file")MultipartFile file,
                                    @RequestParam("fileMd5") String fileMd5,
                                    @RequestParam("chunk") int chunk) throws Exception{
        // 创建一个临时文件
        File tempFile = File.createTempFile("minio", ".temp");
        file.transferTo(tempFile);
        // 文件路径
        String localFilePath = tempFile.getAbsolutePath();

        return bigFilesService.uploadChunk(fileMd5,chunk,localFilePath);
    }

    @ApiOperation(value = "合并文件")
    @PostMapping("/upload/mergechunks")
    public RestResponse mergeChunks(@RequestParam("fileMd5") String fileMd5,
                                    @RequestParam("fileName") String fileName,
                                    @RequestParam("chunkTotal") int chunkTotal) throws Exception {
        Long companyId = 1232141425L;
        //文件信息对象
        UploadFileParamsDto uploadFileParamsDto = new UploadFileParamsDto();
        uploadFileParamsDto.setFilename(fileName);
        uploadFileParamsDto.setTags("视频文件");
        uploadFileParamsDto.setFileType("001002");
        return bigFilesService.mergeChunks(companyId, fileMd5, chunkTotal, uploadFileParamsDto);
    }
}
