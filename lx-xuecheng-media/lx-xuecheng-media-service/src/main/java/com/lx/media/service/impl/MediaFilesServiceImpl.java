package com.lx.media.service.impl;

import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.lx.base.execption.XueChengException;
import com.lx.media.mapper.MediaFilesMapper;
import com.lx.media.mapper.MediaProcessMapper;
import com.lx.media.model.dto.UploadFileParamsDto;
import com.lx.media.model.dto.UploadFileResultDto;
import com.lx.media.model.entity.MediaFiles;
import com.lx.media.model.entity.MediaProcess;
import com.lx.media.service.MediaFilesService;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;

@Service
@Slf4j
public class MediaFilesServiceImpl implements MediaFilesService {

    public static final String OBJECT_APPROVAL = "002003";
    public static final String MEDIA_FILE_NORMAL = "1";
    public static final String AVI_MIMETYPE = "video/x-msvideo";

    @Autowired
    private MinioClient minioClient;
    @Autowired
    private MediaFilesService currentProxy;
    @Autowired
    private MediaProcessMapper mediaProcessMapper;

    @Autowired
    private MediaFilesMapper mediaFilesMapper;

    @Value("${minio.bucket.files}")
    private String bucket_Files;


    @Override
    public MediaFiles getFileById(String mediaId) {
        MediaFiles mediaFiles = mediaFilesMapper.selectById(mediaId);
        return mediaFiles;
    }

    /**
     * @author lx
     * @date 2023/5/23 21:57
     * @description 获取文件默认存储目录路径 年/月/日
     */
    public String getDefaultFolderPath(){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM--dd");
        String folder = format.format(new Date()).replace("-","/")+"/";
        return folder;
    }

    /**
     * @author lx
     * @date 2023/5/23 22:00
     * @param file file文件
     * @description 获取文件的MD5
     */
    public String getFileMd5(File file){
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            String fileMd5 = DigestUtils.md5Hex(fileInputStream);
            return fileMd5;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @author lx
     * @date 2023/5/23 22:03
     * @param extension 扩展名
     * @description 获取mimeType
     */
    public String getMimeType(String extension){
        if (extension == null) {
            extension = "";
        }
        // 根据扩展名取出mimeType
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);
        // 通用mimeType，字节流
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        if (extensionMatch!=null){
            mimeType = extensionMatch.getMimeType();
        }
        return mimeType;
    }

    /**
     * @author lx
     * @date 2023/5/23 22:10
     * @param localFilePath  文件地址
     * @param bucket  桶
     * @param objectName 对象名称
     * @description 将文件写入minIO
     */
    public boolean addMediaFilesToMinIO(String localFilePath,String mimeType,String bucket,String objectName){
        try {
            UploadObjectArgs fileBucket = UploadObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .filename(localFilePath)
                    .contentType(mimeType)
                    .build();
            minioClient.uploadObject(fileBucket);
            log.debug("上传文件到minio成功,bucket:{},objectName:{}",bucket,objectName);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("上传文件到minio出错,bucket:{},objectName:{},错误原因:{}",bucket,objectName,e.getMessage(),e);
            XueChengException.cast("文件上传到文件系统失败");
        }
        return false;
    }

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
    @Transactional
    public MediaFiles addMediaFilesToDb(Long companyId,String fileMd5,UploadFileParamsDto uploadFileParamsDto,String bucket,String objectName){
        //从数据库查询文件
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (mediaFiles == null) {
            mediaFiles = new MediaFiles();
            //拷贝基本信息
            BeanUtils.copyProperties(uploadFileParamsDto, mediaFiles);
            mediaFiles.setId(fileMd5);
            mediaFiles.setFileId(fileMd5);
            mediaFiles.setCompanyId(companyId);
            mediaFiles.setUrl("/" + bucket + "/" + objectName);
            mediaFiles.setBucket(bucket);
            mediaFiles.setFilePath(objectName);
            mediaFiles.setCreateDate(LocalDateTime.now());
            mediaFiles.setAuditStatus(OBJECT_APPROVAL);
            mediaFiles.setStatus(MEDIA_FILE_NORMAL);
            //保存文件信息到文件表
            int insert = mediaFilesMapper.insert(mediaFiles);
            if (insert < 0) {
                log.error("保存文件信息到数据库失败,{}",mediaFiles.toString());
                XueChengException.cast("保存文件信息失败");
            }
            // 添加到待处理任务
            addWaitingTask(mediaFiles);
            log.debug("保存文件信息到数据库成功,{}",mediaFiles.toString());
        }
        return mediaFiles;
    }

    /**
     * 添加待处理任务
     * @param mediaFiles 媒资文件信息
     */
    private void addWaitingTask(MediaFiles mediaFiles){
        //文件名称
        String filename = mediaFiles.getFilename();
        //文件扩展名
        String exension = filename.substring(filename.lastIndexOf("."));
        //文件mimeType
        String mimeType = getMimeType(exension);
        //如果是avi视频添加到视频待处理表
        if(mimeType.equals(AVI_MIMETYPE)){
            MediaProcess mediaProcess = new MediaProcess();
            BeanUtils.copyProperties(mediaFiles,mediaProcess);
            mediaProcess.setStatus("1");//未处理
            mediaProcess.setFailCount(0);//失败次数默认为0
            mediaProcessMapper.insert(mediaProcess);
        }
    }

    @Override
    public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, String localFilePath) {

        File file = new File(localFilePath);
        if (!file.exists()){
            XueChengException.cast("文件不存在");
        }
        // 文件名
        String filename = uploadFileParamsDto.getFilename();
        // 文件扩展名
        String extension = filename.substring(filename.lastIndexOf("."));
        // 文件mimeType
        String mimeType = getMimeType(extension);
        // 文件的md5值
        String fileMd5 = getFileMd5(file);
        // 文件默认目录
        String defaultFolderPath = getDefaultFolderPath();
        // 存储到minio中的对象名(带目录)
        String objectName = defaultFolderPath+fileMd5+extension;
        // 将文件上传到minio
        boolean b = addMediaFilesToMinIO(localFilePath, mimeType, bucket_Files, objectName);
        // 文件大小
        uploadFileParamsDto.setFileSize(file.length());
        // 将文件存储到数据库
        MediaFiles mediaFiles = currentProxy.addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, bucket_Files, objectName);
        // 准备返回数据
        UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
        BeanUtils.copyProperties(mediaFiles,uploadFileResultDto);
        return uploadFileResultDto;
    }
}
