package com.lx.media.service;


import com.lx.media.model.dto.UploadFileParamsDto;
import com.lx.media.model.dto.UploadFileResultDto;
import com.lx.media.model.entity.MediaFiles;

public interface MediaFilesService {

    /**
     * @author lx
     * @date 2023/5/24 9:34
     * @param company  机构id
     * @param uploadFileParamsDto  文件对象
     * @param localFilePath 本地文件地址
     * @description
     */
    public UploadFileResultDto uploadFile(Long company, UploadFileParamsDto uploadFileParamsDto,String localFilePath);

    /**
     * @author lx
     * @date 2023/5/23 22:23
     * @param companyId  机构id
     * @param fileMd5  文件md5值
     * @param uploadFileParamsDto  上传文件的信息
     * @param bucket  桶
     * @param objectName 对象名称
     * @description 将文件信息添加到文件表
     */
    public MediaFiles addMediaFilesToDb(Long companyId, String fileMd5, UploadFileParamsDto uploadFileParamsDto, String bucket, String objectName);

    //根据媒资id查询文件信息
    MediaFiles getFileById(String mediaId);
}
