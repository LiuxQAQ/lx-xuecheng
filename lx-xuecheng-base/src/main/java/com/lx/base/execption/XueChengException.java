package com.lx.base.execption;

/**
 * @author lx
 * @date 2023/5/19 20:06
 * @description 学成在线项目异常类
 */
public class XueChengException extends RuntimeException{

    private String errMessage;

    public XueChengException(){
        super();
    }

    public XueChengException(String errMessage){
        super(errMessage);
        this.errMessage = errMessage;
    }

    public String getErrMessage(){
        return errMessage;
    }

    public static void cast(CommonError commonError){
        throw new XueChengException(commonError.getErrMessage());
    }

    public static void cast(String errMessage){
        throw new XueChengException(errMessage);
    }
}
