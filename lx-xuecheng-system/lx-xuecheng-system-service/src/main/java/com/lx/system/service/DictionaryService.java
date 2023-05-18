package com.lx.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lx.system.model.entity.Dictionary;

import java.util.List;

/**
 * <p>
 * 数据字典 服务类
 * </p>
 *
 * @author itcast
 * @since 2023-05-17
 */
public interface DictionaryService extends IService<Dictionary> {

    /**
     * @author lx
     * @date 2023/5/17 22:39
     * @param
     * @description 查询所有数据字典内容
     */
    public List<Dictionary> queryAll();

    /**
     * @author lx
     * @date 2023/5/17 22:41
     * @param code -- String数据字典Code
     * @description 根据code查询数据字典
     */
    public Dictionary getByCode(String code);
}
