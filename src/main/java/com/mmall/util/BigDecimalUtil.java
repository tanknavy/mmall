package com.mmall.util;

import java.math.BigDecimal;

/**
 * Created by Alex Cheng
 * 4/26/2020 11:39 AM
 */
public class BigDecimalUtil {

    private BigDecimalUtil(){
        //私有构造器，不可以在外面new
    }

    //DB中数据是float类型，在价格计算中使用Bigdecimal,需要先转double为string
    public static BigDecimal add(double v1, double v2){
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));

        return b1.add(b2);
    }

    public static BigDecimal sub(double v1, double v2){
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));

        return b1.subtract(b2);
    }

    public static BigDecimal mul(double v1, double v2){
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));

        return b1.multiply(b2);
    }

    public static BigDecimal div(double v1, double v2){
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));

        //return b1.divide(b2);
        // 除不净的情况，采用某个策略
        return b1.divide(b2,2,BigDecimal.ROUND_HALF_UP); //保留两位小数，四舍五入！
    }
}
