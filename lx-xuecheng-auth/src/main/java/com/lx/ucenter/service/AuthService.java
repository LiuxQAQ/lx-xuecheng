package com.lx.ucenter.service;

import com.lx.ucenter.model.dto.AuthParamsDto;
import com.lx.ucenter.model.dto.XcUserExt;

public interface AuthService {

    /**
     * @author lx
     * @date 2023/5/31 10:25
     * @param authParamsDto 认证参数
     * @description 认准方法
     
     */
    public XcUserExt execute(AuthParamsDto authParamsDto);
}
