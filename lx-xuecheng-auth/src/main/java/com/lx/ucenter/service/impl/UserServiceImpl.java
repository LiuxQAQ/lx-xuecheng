package com.lx.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lx.ucenter.mapper.XcMenuMapper;
import com.lx.ucenter.mapper.XcUserMapper;
import com.lx.ucenter.model.dto.AuthParamsDto;
import com.lx.ucenter.model.dto.XcUserExt;
import com.lx.ucenter.model.entity.XcMenu;
import com.lx.ucenter.model.entity.XcUser;
import com.lx.ucenter.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lx
 * @date 2023/5/31 9:12
 * @description TODO

 */
@Service
@Slf4j
public class UserServiceImpl implements UserDetailsService {

    @Autowired
    private XcUserMapper userMapper;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private XcMenuMapper menuMapper;


    /**
     * @author lx
     * @date 2023/5/31 9:14
     * @param s 账号
     * @description 根据账号查询用户信息
     */
    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {

        AuthParamsDto authParamsDto = null;
        try {
            //将认证参数转为AuthParamsDto类型
            authParamsDto = JSON.parseObject(s, AuthParamsDto.class);
        } catch (Exception e) {
            log.info("认证请求不符合项目要求:{}",s);
            throw new RuntimeException("认证请求数据格式不对");
        }
        // 认证方法
        String authType = authParamsDto.getAuthType();
        AuthService authService = applicationContext.getBean(authType + "_authService", AuthService.class);
        XcUserExt user = authService.execute(authParamsDto);
        return getUserPrincipal(user);
    }

    // 查询用户身份
    public UserDetails getUserPrincipal(XcUserExt user){
        String password = user.getPassword();
        // 查询用户权限
        List<XcMenu> xcMenus = menuMapper.selectPermissionByUserId(user.getId());
        List<String> permissions = new ArrayList<>();
        if (xcMenus.size()<=0){
            permissions.add("p1");
        }else {
            xcMenus.forEach(menu->{
                permissions.add(menu.getCode());
            });
        }
        // 将用户权限放在XcUserExt中
        user.setPermissions(permissions);
        //为了安全在令牌中不放密码
        user.setPassword(null);
        //将user对象转json
        String userString = JSON.toJSONString(user);
        String[] authorities = permissions.toArray(new String[0]);
        //创建UserDetails对象
        UserDetails userDetails = User.withUsername(userString).password(password ).authorities(authorities).build();
        return userDetails;
    }
}
