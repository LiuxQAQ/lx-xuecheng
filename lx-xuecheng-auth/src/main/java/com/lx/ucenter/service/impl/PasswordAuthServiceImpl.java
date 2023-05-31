package com.lx.ucenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lx.ucenter.feignclient.CheckCodeClient;
import com.lx.ucenter.mapper.XcUserMapper;
import com.lx.ucenter.model.dto.AuthParamsDto;
import com.lx.ucenter.model.dto.XcUserExt;
import com.lx.ucenter.model.entity.XcUser;
import com.lx.ucenter.service.AuthService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.swing.*;


/**
 * @author lx
 * @date 2023/5/31 14:06
 * @description 账号密码认证
 */
@Service("password_authService")
public class PasswordAuthServiceImpl implements AuthService {

     @Autowired
     private XcUserMapper xcUserMapper;
     @Autowired
     private PasswordEncoder passwordEncoder;
     @Autowired
     private CheckCodeClient checkCodeClient;


     @Override
     public XcUserExt execute(AuthParamsDto authParamsDto) {

        // 校验验证码
      String checkcode = authParamsDto.getCheckcode();
      String checkcodekey = authParamsDto.getCheckcodekey();

      if (StringUtils.isBlank(checkcode) || StringUtils.isBlank(checkcodekey)){
          throw new RuntimeException("验证码为空");
      }
      Boolean verify = checkCodeClient.verify(checkcodekey, checkcode);
      if (!verify){
       throw new RuntimeException("验证码输入错误");
      }
      //账号
      String username = authParamsDto.getUsername();
      XcUser user = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getUsername, username));
      if(user==null){
        //返回空表示用户不存在
        throw new RuntimeException("账号不存在");
      }
      XcUserExt xcUserExt = new XcUserExt();
      BeanUtils.copyProperties(user,xcUserExt);
      //校验密码
      //取出数据库存储的正确密码
      String passwordDb  =user.getPassword();
      String passwordForm = authParamsDto.getPassword();
      boolean matches = passwordEncoder.matches(passwordForm, passwordDb);
      if(!matches){
       throw new RuntimeException("账号或密码错误");
      }
      return xcUserExt;
     }
}