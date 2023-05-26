package com.lx.media.service.jobhandler;


import com.lx.base.utils.Mp4VideoUtil;
import com.lx.media.model.entity.MediaProcess;
import com.lx.media.service.BigFilesService;
import com.lx.media.service.MediaFilesService;
import com.lx.media.service.MediaProcessService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author lx
 * @date 2023/5/26 14:27
 * @description 视频处理任务
 */

@Slf4j
@Component
public class VideoTask {

    public static final String AVI_MIMETYPE = "video/x-msvideo";

    @Autowired
    private MediaProcessService mediaProcessService;
    @Autowired
    private MediaFilesService mediaFilesService;
    @Autowired
    private BigFilesService bigFilesService;

    @Value("${videoprocess.ffmpegpath}")
    private String ffmpegpath;

    @XxlJob("videoJobHandler")
    public void videoJobHandler() throws Exception{
        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        List<MediaProcess> mediaProcessList = null;
        int size = 0;
        try {
            // 取出cpu核心数 作为一次处理数据的条数
            int processors = Runtime.getRuntime().availableProcessors();
            //获取待处理的任务 一次处理视频数量不超过cpu核心数
            mediaProcessList = mediaProcessService.getMediaProcessList(shardIndex, shardTotal, processors);
            size = mediaProcessList.size();
            log.debug("取出待处理视频任务{}条",size);
            if (size < 0){
                return;
            }
        }catch (Exception e){
            e.printStackTrace();
            return;
        }

        // 启动size个线程的线程池
        ExecutorService threadPool = Executors.newFixedThreadPool(size);
        // 计数器
        CountDownLatch countDownLatch = new CountDownLatch(size);
        // 将处理任务加入到线程池
        mediaProcessList.forEach(mediaProcess -> {
            threadPool.execute(() -> {
                try {
                    // 任务id
                    Long id = mediaProcess.getId();
                    // 抢占任务
                    boolean b = mediaProcessService.startTask(id);
                    if (!b){
                        return;
                    }
                    log.debug("开始执行任务:{}",mediaProcess);
                    // 下边是处理逻辑
                    // 桶
                    String bucket = mediaProcess.getBucket();
                    // 存储路径
                    String filePath = mediaProcess.getFilePath();
                    // 原始视频的md5值
                    String fileId = mediaProcess.getFileId();
                    // 原始文件名称
                    String filename = mediaProcess.getFilename();
                    // 将要处理的文件下载到服务器上
                    File originalFile = bigFilesService.downloadFileFromMinIO(bucket, filePath);
                    if (originalFile == null){
                        log.debug("下载待处理文件失败，originalFile:{}",
                                mediaProcess.getBucket().concat(filePath));
                        mediaProcessService.saveProcessFinishStatus(mediaProcess.getId(),"3",fileId,null,"下载待处理文件失败");
                        return;
                    }
                    // 处理结束视频文件
                    File mp4File = null;
                    try {
                        mp4File = File.createTempFile("mp4",".mp4");
                    }catch (Exception e){
                        log.error("创建mp4临时文件失败");
                        mediaProcessService.saveProcessFinishStatus(id,"3",fileId,null,"创建临时mp4文件失败");
                        return;
                    }
                    // 视频处理结果
                    String result = "";
                    try {
                        // 开始处理视频
                        Mp4VideoUtil videoUtil = new Mp4VideoUtil(ffmpegpath, originalFile.getAbsolutePath(),
                                mp4File.getName(), mp4File.getAbsolutePath());
                        // 开始视频转换，成功将返回success
                        result = videoUtil.generateMp4();
                    }catch (Exception e){
                        e.printStackTrace();
                        log.error("处理视频失败,视频地址:{},错误信息:{}",bucket+filePath,result);
                        mediaProcessService.saveProcessFinishStatus(id,"3",fileId,null,result);
                        return;
                    }

                    //================== 将mp4上传至minio====================
                    // mp4在minio的存储路径
                    String objectName = getFilePath(fileId, ".mp4");
                    // 访问url
                    String url = "/" + bucket + "/" + objectName;
                    try {
                        bigFilesService.addMediaFilesToMinIO(mp4File.getAbsolutePath(),AVI_MIMETYPE,bucket,objectName);
                        // 将url存储至数据，并更新状态为成功，并将待处理视频记录删除存入历史
                        mediaProcessService.saveProcessFinishStatus(id,"2",fileId,url,null);
                    }catch (Exception e){
                        log.error("上传视频失败或入库失败，视频地址:{},错误信息:{}",bucket + objectName,e.getMessage());
                        // 最终还是失败了
                        mediaProcessService.saveProcessFinishStatus(mediaProcess.getId(), "3", fileId, null, "处理后视频上传或入库失败");
                    }
                }finally {
                    countDownLatch.countDown();
                }
            });
        });
        // 等待，给一个充裕的超时时间，防止无限等待，到达超时时间还没有处理完成则结束任务
        countDownLatch.await(30, TimeUnit.MINUTES);
    }

    // 获得文件的地址
    private String getFilePath(String fileMd5,String fileExt){
        return   fileMd5.substring(0,1) + "/" + fileMd5.substring(1,2) + "/" + fileMd5 + "/" +fileMd5 +fileExt;
    }


    //如果有线程抢占了某个视频的处理任务，如果线程处理过程中挂掉了，该视频的状态将会一直是处理中，其它线程将无法处理，这个问题需要用补偿机制。
    //单独启动一个任务找到待处理任务表中超过执行期限但仍在处理中的任务，将任务的状态改为执行失败。
    // todo 任务补偿

}
