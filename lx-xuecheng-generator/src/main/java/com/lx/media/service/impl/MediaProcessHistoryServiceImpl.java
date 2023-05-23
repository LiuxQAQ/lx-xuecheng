package com.lx.media.service.impl;

import com.lx.media.model.entity.MediaProcessHistory;
import com.lx.media.mapper.MediaProcessHistoryMapper;
import com.lx.media.service.MediaProcessHistoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author itcast
 */
@Slf4j
@Service
public class MediaProcessHistoryServiceImpl extends ServiceImpl<MediaProcessHistoryMapper, MediaProcessHistory> implements MediaProcessHistoryService {

}
