package com.lx.media;

import com.spring4all.swagger.EnableSwagger2Doc;
import kotlin.time.MeasureTimeKt;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableSwagger2Doc
@MapperScan("com.lx.media.mapper")
// 为了扫描 全局异常处理
@ComponentScan(basePackages = "com.lx")
public class MediaApplication {
    public static void main(String[] args) {
        SpringApplication.run(MediaApplication.class,args);
    }
}
