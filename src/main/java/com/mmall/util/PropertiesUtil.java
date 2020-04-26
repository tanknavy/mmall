package com.mmall.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * Created by alex
 */
public class PropertiesUtil {

    private static Logger logger = LoggerFactory.getLogger(PropertiesUtil.class);

    private static Properties props; //静态常量


    // 静态代码块，只执行一次，在类被加载的时候执行，一般用户初始化静态变量，执行顺序 静态代码块>普通代码块>构造代码块
    // 当这个类被java的class loader加载进入时就执行一次
    static {
        String fileName = "mmall.properties"; //resources目录下
        props = new Properties();
        try {
            props.load(new InputStreamReader(PropertiesUtil.class.getClassLoader().getResourceAsStream(fileName),"UTF-8"));
        } catch (IOException e) {
            logger.error("配置文件读取异常",e);
        }
    }

    //工具类，静态方法
    public static String getProperty(String key){
        String value = props.getProperty(key.trim()); //前后trim空格
        if(StringUtils.isBlank(value)){
            return null;
        }
        return value.trim();
    }

    public static String getProperty(String key,String defaultValue){ //方法重载

        String value = props.getProperty(key.trim());
        if(StringUtils.isBlank(value)){
            value = defaultValue;
        }
        return value.trim();
    }

    /*
    public static void main(String[] args) throws ClassNotFoundException {
        Class.forName("com.mysql.jdbc.Driver"); //Driver中就是static代码块
    }*/

}
