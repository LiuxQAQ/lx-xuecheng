package com.lx.auth.controller;

import com.lx.ucenter.mapper.XcUserMapper;
import com.lx.ucenter.model.entity.XcUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

public class LoginController {

    @Autowired
    private XcUserMapper userMapper;

    @RequestMapping("/login-success")
    public String loginSuccess(){
        return "登录成功!";
    }

    @RequestMapping("/user/{id}")
    public XcUser getuser(@PathVariable("id") String id){
        XcUser xcUser = userMapper.selectById(id);
        return xcUser;
    }

    @RequestMapping("/r/r1")
    @PreAuthorize("hasAuthority('p1')") // 拥有p1权限方可访问
    public String r1(){
        return "访问r1资源";
    }

    @RequestMapping("/r/r2")
    @PreAuthorize("hasAuthority('p2')")
    public String r2(){
        return "访问r2资源";
    }

}
