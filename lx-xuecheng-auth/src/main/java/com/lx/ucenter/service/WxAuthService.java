package com.lx.ucenter.service;


import com.lx.ucenter.model.entity.XcUser;

/**
 * @author lx
 * @date 2023/5/31 15:48
 * @description 微信认证接口
 */
public interface WxAuthService {

    public XcUser wxAuth(String code);

}
