package com.mmall.common;

import com.mmall.pojo.User;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.Serializable;

/*
通用响应对象,后端接口返回json用户信息给前端
 */
//序列化到json时，不包含空值节点
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ServerResponse<T> implements Serializable {

    private int status;
    private String msg;
    private T data; //返回对象

    private ServerResponse(int status){
        this.status = status;
    }

    private ServerResponse(String msg, T data) {
        this.msg = msg;
        this.data = data;
    }

    private ServerResponse(int status, T data) {
        this.status = status;
        this.data = data;
    }

    // 这两个构造器会不会冲突？
    private ServerResponse(int status, String msg) {
        this.status = status;
        this.msg = msg;
    }

    private ServerResponse(int status, String msg, T data) {
        this.status = status;
        this.msg = msg;
        this.data = data;
    }

    @JsonIgnore // 公共方法，避免这个布尔字段出现在序列化中
    public boolean isSuccess(){
        return this.status == ResponseCode.SUCCESS.getCode(); // 枚举类，杜绝硬编码
    }

    public int getStatus() {
        return status;
    }

    public String getMsg() {
        return msg;
    }

    public T getData() {
        return data;
    }

    // 静态方法不能引用非静态的类型,所以加上<T>,静态方法一般用于工具类、工厂类创建自身对象
    public static <T> ServerResponse<T> createBySuccess(){
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode());
    }

    public static <T> ServerResponse<T> createBySuccessMessage(String msg){
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode(),msg);
    }

    public static <T> ServerResponse<T> createBySuccess(T data){
        return new ServerResponse<>(ResponseCode.SUCCESS.getCode(),data);
    }

    public static <T> ServerResponse<T> createBySuccessMessage(String msg, T data){
        return new ServerResponse<>(ResponseCode.SUCCESS.getCode(), msg, data);
    }

    public static <T> ServerResponse<T> createByError(){
        return new ServerResponse<T>(ResponseCode.ERROR.getCode(), ResponseCode.ERROR.getDesc());
    }

    public static <T> ServerResponse<T> createByErrorMessage(String errorMessage){
        return new ServerResponse<T>(ResponseCode.ERROR.getCode(), errorMessage);
    }

    public static <T> ServerResponse<T> createByErrorMessage(int errorCode, String errorMessage){
        return new ServerResponse<>(errorCode, errorMessage);
    }

    // 参数校验错误时返回参数
    /*
    public static<T> ServerResponse<T> createByErrorCodeMessage(int errorCode, T data){
        return new ServerResponse<>(ResponseCode.ERROR.getCode(), data);
    }*/
}
