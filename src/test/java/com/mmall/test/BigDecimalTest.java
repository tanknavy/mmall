package com.mmall.test;

import org.junit.Test;

import java.math.BigDecimal;

/**
 * Created by Alex Cheng
 * 4/26/2020 11:26 AM
 */
public class BigDecimalTest {
    @Test
    public void test1(){
        System.out.println(0.05+0.01);
        System.out.println(1.0-0.35);
        System.out.println(4.015*100);
        System.out.println(123.3/100);
    }

    @Test
    public void test2(){
        BigDecimal b1 = new BigDecimal(0.05);
        BigDecimal b2 = new BigDecimal(0.01);
        System.out.println(b1.add(b2)); //结果更乱了

    }

    @Test
    public void test3(){ // 一定要使用Bigdecimal的string构造器方法
        BigDecimal b1 = new BigDecimal("0.05"); //使用string constructor方法
        BigDecimal b2 = new BigDecimal("0.01");
        System.out.println(b1.add(b2)); //结果更乱了

    }
}
