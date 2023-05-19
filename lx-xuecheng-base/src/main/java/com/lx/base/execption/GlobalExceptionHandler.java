package com.lx.base.execption;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


/**
 * @author lx
 * @date 2023/5/19 20:24
 * @description 全局异常处理器
 */
@Slf4j
@Order(9999)
// 对 Spring 架构和 SpringMVC 的Controller 的异常捕获提供了相应的异常处理。
@RestControllerAdvice
public class GlobalExceptionHandler {


    /**
     * @author lx
     * @date 2023/5/19 21:53
     * @description XueChengException异常捕获并返回
     */
    @ResponseBody
    // Spring3.0提供的标识在方法上或类上的注解，用来表明方法的处理异常类型。
    @ExceptionHandler(XueChengException.class)
    // Spring3.0提供的标识在方法上或类上的注解，用状态代码和应返回的原因标记
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse customException(XueChengException e){
        System.out.println("*****************************");
        log.error("【系统异常】{}",e.getErrMessage(),e);
        return new RestErrorResponse(e.getErrMessage());
    }

    /**
     * @author lx
     * @date 2023/5/19 21:54
     * @description 其他异常捕获并返回
     */
    @ResponseBody
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse exception(Exception e){
        log.error("【系统异常】{}",e.getMessage(),e);
        return new RestErrorResponse(CommonError.UNKNOWN_ERROR.getErrMessage());
    }

    /**
     * @author lx
     * @date 2023/5/19 21:55
     * @description JRS303校验异常捕获并返回
     */
    @ResponseBody
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse methodArgumentNotValidException(MethodArgumentNotValidException e){
        BindingResult bindingResult = e.getBindingResult();
        List<String> msgList = new ArrayList<>();
        // 将错误信息放在msgList中
        bindingResult.getFieldErrors().stream()
                .forEach(item->msgList.add(item.getDefaultMessage()));
        // 拼接错误信息
        String msg = StringUtils.join(msgList,",");
        log.error("【系统异常】{}",msg);
        return new RestErrorResponse(msg);
    }



}
