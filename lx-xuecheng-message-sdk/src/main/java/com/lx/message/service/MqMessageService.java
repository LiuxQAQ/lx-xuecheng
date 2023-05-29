package com.lx.message.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lx.message.model.entity.MqMessage;

import java.util.List;


/**
 * <p>
 *  服务类
 * </p>
 *
 * @author itcast
 * @since 2023-05-28
 */
public interface MqMessageService  {
    /**
     * @description 扫描消息表记录，采用与扫描视频处理表相同的思路
     * @param shardIndex 分片序号
     * @param shardTotal 分片总数
     * @param count 扫描记录数
     * @return java.util.List 消息记录
     * @author lx
     * @date 2023/5/28 16:21
     */
    public List<MqMessage> getMessageList(int shardIndex, int shardTotal, String messageType, int count);

    /**
     * @description 完成任务
     * @param id 消息id
     * @return int 更新成功：1
     * @author lx
     * @date 2023/5/28 16:21
     */
    public int completed(long id);

    /**
     * @description 完成阶段任务
     * @param id 消息id
     * @return int 更新成功：1
     * @author lx
     * @date 2023/5/28 16:21
     */
    public int completedStageOne(long id);
    public int completedStageTwo(long id);
    public int completedStageThree(long id);
    public int completedStageFour(long id);

    /**
     * @description 查询阶段状态
     * @param id
     * @return int
     * @author lx
     * @date 2023/5/28 16:21
     */
    public int getStageOne(long id);
    public int getStageTwo(long id);
    public int getStageThree(long id);
    public int getStageFour(long id);

    /**
     * @author lx
     * @date 2023/5/28 16:56
     * @param messageType 消息类型
     * @param businessKey1 业务id
     * @param businessKey2 业务id
     * @param businessKey3 业务id
     * @param messageType 消息类型
     * @description 添加消息
     */
    public MqMessage addMessage(String messageType,String businessKey1,String businessKey2,String businessKey3);

}
