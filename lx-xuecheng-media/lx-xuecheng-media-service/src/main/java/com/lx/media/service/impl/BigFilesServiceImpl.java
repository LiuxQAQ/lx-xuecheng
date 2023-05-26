package com.lx.media.service.impl;

import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.lx.base.execption.XueChengException;
import com.lx.base.model.RestResponse;
import com.lx.media.mapper.MediaFilesMapper;
import com.lx.media.model.dto.UploadFileParamsDto;
import com.lx.media.model.entity.MediaFiles;
import com.lx.media.service.BigFilesService;
import com.lx.media.service.MediaFilesService;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import jdk.internal.util.xml.impl.Input;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class BigFilesServiceImpl implements BigFilesService {
    
    @Autowired
    private MediaFilesMapper mediaFilesMapper;
    @Autowired
    private MinioClient minioClient;
    @Autowired
    private MediaFilesService currentProxy;

    @Value("${minio.bucket.videofiles}")
    private String bucket_video;
    
    @Override
    public RestResponse<Boolean> checkFile(String fileMd5) {
        // 查询文件信息
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (mediaFiles != null){
            // 桶
            String bucket = mediaFiles.getBucket();
            // 存储目录
            String filePath = mediaFiles.getFilePath();
            // 文件流
            InputStream stream = null;
            try {
                stream = minioClient.getObject(GetObjectArgs.builder().bucket(bucket).object(filePath).build());
                if (stream != null){
                    // 文件已经存在
                    return RestResponse.success(true);
                }
            }catch (Exception e){
            }
        }
        return RestResponse.success(false);
    }

    @Override
    public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex) {
        // 得到分块文件目录
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        // 得到分块文件的路径
        String chunkFilePath = chunkFileFolderPath + chunkIndex;
        //如果数据库存在再查询 minio
        GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                .bucket(bucket_video)
                .object(chunkFileFolderPath+chunkIndex)
                .build();
        //查询远程服务获取到一个流对象
        try {
            FilterInputStream inputStream = minioClient.getObject(getObjectArgs);
            if(inputStream!=null){
                //文件已存在
                return RestResponse.success(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return RestResponse.success(false);
    }

    @Override
    public RestResponse<Boolean> uploadChunk(String fileMd5, int chunk, String localChunkFilePath) {
        // 分块文件路径
        String chunkFilePath = getChunkFileFolderPath(fileMd5) + chunk;
        // 获取mimeType
        String mimeType = getMimeType(null);
        // 将分块文件上传到minio
        boolean b = addMediaFilesToMinIO(localChunkFilePath, mimeType, bucket_video, chunkFilePath);
        if (!b){
            return RestResponse.validfail(false,"上传分块文件失败");
        }
        return RestResponse.success(true);
    }

    @Override
    public RestResponse<Boolean> mergeChunks(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto) {
        // 分块文件所在目录
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        // 找到所有分区文件
        List<ComposeSource> sources = Stream.iterate(0, i -> ++i)
                .limit(chunkTotal)
                .map(i -> ComposeSource.builder().bucket(bucket_video).object(chunkFileFolderPath + i).build())
                .collect(Collectors.toList());
        // 源文件名称
        String filename = uploadFileParamsDto.getFilename();
        // 扩展名
        String extension = filename.substring(filename.lastIndexOf("."));
        // 合并后文件的objectname
        String objectName = getFilePathByMd5(fileMd5, extension);
        // 指定合并后的objectName等信息
        ComposeObjectArgs composeObjectArgs = ComposeObjectArgs.builder()
                .bucket(bucket_video)
                .object(objectName)
                .sources(sources)
                .build();
        //===================合并文件===========================
        try {
            minioClient.composeObject(composeObjectArgs);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("合并文件出错,bucket:{},objectName:{},错误信息:{}", bucket_video, objectName, e.getMessage());
            return RestResponse.validfail(false, "合并文件异常");
        }
        //===========校验合并后的和源文件是否一致，视频上传才成功===========
        //先下载合并后的文件
        File file = downloadFileFromMinIO(bucket_video, objectName);
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            //计算合并后文件的md5
            String mergeFile_md5 = DigestUtils.md5Hex(fileInputStream);
            //比较原始md5和合并后文件的md5
            if (!fileMd5.equals(mergeFile_md5)) {
                log.error("校验合并文件md5值不一致,原始文件:{},合并文件:{}", fileMd5, mergeFile_md5);
                return RestResponse.validfail(false, "文件校验失败");
            }
            //文件大小
            uploadFileParamsDto.setFileSize(file.length());
        } catch (Exception e) {
            return RestResponse.validfail(false, "文件校验失败");
        }
        //==============将文件信息入库============
        MediaFiles mediaFiles = currentProxy.addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, bucket_video, objectName);
        if(mediaFiles == null){
            return RestResponse.validfail(false,"文件入库失败");
        }
        //==========清理分块文件=========
        clearChunkFiles(chunkFileFolderPath,chunkTotal);

        return RestResponse.success(true);
    }
    /**
     * 清除分块文件
     * @param chunkFileFolderPath 分块文件路径
     * @param chunkTotal 分块文件总数
     */
    private void clearChunkFiles(String chunkFileFolderPath,int chunkTotal){
        Iterable<DeleteObject> objects =  Stream.iterate(0, i -> ++i).limit(chunkTotal).map(i -> new DeleteObject(chunkFileFolderPath+ i)).collect(Collectors.toList());;
        RemoveObjectsArgs removeObjectsArgs = RemoveObjectsArgs.builder().bucket(bucket_video).objects(objects).build();
        Iterable<Result<DeleteError>> results = minioClient.removeObjects(removeObjectsArgs);
        //要想真正删除
        results.forEach(f->{
            try {
                DeleteError deleteError = f.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }
    /**
     * 从minio下载文件
     * @param bucket 桶
     * @param objectName 对象名称
     * @return 下载后的文件
     */
    public File downloadFileFromMinIO(String bucket,String objectName){
        //临时文件
        File minioFile = null;
        FileOutputStream outputStream = null;
        try{
            InputStream stream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build());
            //创建临时文件
            minioFile=File.createTempFile("minio", ".merge");
            outputStream = new FileOutputStream(minioFile);
            IOUtils.copy(stream,outputStream);
            return minioFile;
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(outputStream!=null){
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
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
     * 得到合并后的文件的地址
     * @param fileMd5 文件id即md5值
     * @param fileExt 文件扩展名
     * @return
     */
    private String getFilePathByMd5(String fileMd5,String fileExt){
        return   fileMd5.substring(0,1) + "/" + fileMd5.substring(1,2) + "/" + fileMd5 + "/" +fileMd5 +fileExt;
    }

    // 得到文件的分块目录
    private String getChunkFileFolderPath(String fileMd5) {
        return fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" + "chunk" + "/";
    }

    /**
     * @author lx
     * @date 2023/5/23 22:03
     * @param extension 扩展名
     * @description 获取mimeType
     */
    private String getMimeType(String extension){
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
}
