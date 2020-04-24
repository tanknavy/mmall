package com.mmall.common;

//常量类
public class Const {
    public static final String CURRENT_USER = "currentUser"; //
    public static final String EMAIL = "email";
    public static final String USERNAME = "username";
    public static final String PASSWORD_RULE= "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{6,16}$";


    public interface Role{ //类似enum类型，但没那么重，多个interface还可以形成分组
        int ROLE_CUSTOMER = 0; //自动final修饰
        int ROLE_ADMIN =1;
    }



}
