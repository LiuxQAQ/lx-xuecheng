package com.lx.content;

import com.spring4all.swagger.EnableSwagger2Doc;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableSwagger2Doc
@MapperScan({"com.lx.content.mapper","com.lx.message.mapper"})
// 为了扫描 全局异常处理
@ComponentScan(basePackages = "com.lx")
@EnableFeignClients(basePackages = {"com.lx.content.feignclient"})
public class ContentApplication {
    public static void main(String[] args) {
        SpringApplication.run(ContentApplication.class,args);
    }
}
