package com.lx.media.service;

import com.lx.base.model.RestResponse;
import com.lx.media.model.dto.UploadFileParamsDto;
import com.lx.media.model.entity.MediaFiles;

import java.io.File;

public interface BigFilesService {

    /**
     * @description 检查文件是否存在
     * @param fileMd5 文件的md5
     * @return false不存在，true存在
     * @author lx
     * @date 2023/5/24
     */
    public RestResponse<Boolean> checkFile(String fileMd5);

    /**
     * @description 检查分块是否存在
     * @param fileMd5  文件的md5
     * @param chunkIndex  分块序号
     * @return false不存在，true存在
     * @author lx
     * @date 2023/5/24
     */
    public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex);

    /**
     * @description 上传分块
     * @param fileMd5  文件md5
     * @param chunk  分块序号
     * @param localChunkFilePath  分块文件本地路径
     * @return false不存在，true存在
     * @author lx
     * @date 2023/5/24
     */
    public RestResponse<Boolean> uploadChunk(String fileMd5,int chunk,String localChunkFilePath);


    /**
     * @description 合并分块
     * @param companyId  机构id
     * @param fileMd5  文件md5
     * @param chunkTotal 分块总和
     * @param uploadFileParamsDto 文件信息
     * @return false不存在，true存在
     * @author lx
     * @date 2023/5/24
     */
    public RestResponse<Boolean> mergeChunks(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto);

    /**
     * @author lx
     * @date 2023/5/26 15:10
     * @param bucket 桶
     * @param objectName 文件名
     * @description 从minio下载文件
     */
    public File downloadFileFromMinIO(String bucket, String objectName);

    /**
     * @author lx
     * @date 2023/5/26 15:46
     * @description 将文件存入minio
     */
    public boolean addMediaFilesToMinIO(String localFilePath,String mimeType,String bucket,String objectName);
}
