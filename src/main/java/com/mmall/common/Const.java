package com.mmall.common;

import com.google.common.collect.Sets;

import java.util.Set;

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

    public interface ProductListOrderBy{
        Set<String> PRICE_ASC_DESC = Sets.newHashSet("price_desc","price_asc");
    }

    public interface Cart{
        int CHECKED =1;//购物车中选状态
        int UN_CHECKED =0;//购物车中未选状态

        String LIMIT_NUM_FAIL = "LIMIT_NUM_FAIL"; //购物车中购买数据限制失败
        String LIMIT_NUN_SUCCESS = "LIMIT_NUM_SUCCESS";
    }

    public enum ProductStatusEnum{
        ON_SALE(1,"on sales");

        private String value;
        private int code;
        ProductStatusEnum(int code,String value){
            this.value = value;
            this.code = code;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }
    }

    public enum OrderStatusEnum{
        CANCELED(0,"Canceled"),
        NO_PAY(10,"Not Pay"),
        PAID(20,"Paid"),
        SHIPPED(40,"Shipped"),
        ORDER_SUCCESS(50,"Order Success"),
        ORDER_CLOSE(60,"Order Closed");

        OrderStatusEnum(int code, String value){
            this.code = code;
            this.value = value;
        }

        private String value;
        private int code;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }
    }

    public interface AlipayCallback{
        String TRADE_STATUS_WAIT_BUYER_PAY = "WAIT_BUYER_PAY";
        String TRADE_STATUS_TRADE_SUCCESS = "TRADE_SUCCESS";

        // alipay callback required return result
        String RESPONSE_SUCCESS = "success";
        String RESPONSE_FAILED = "failed";
    }

    public enum PayPlatformEnum{
        ALIPAY(1,"Alipay"),
        WEICHATPAY(2,"WeiChatPay");

        private int code;
        private String value;
        PayPlatformEnum(int code,String value){
            this.value = value;
            this.code = code;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }
    }


    public enum PaymentTypeEnum{

        ONLINE_PAY(1, "Online Payment");
        private int code;
        private String value;

        PaymentTypeEnum(int code,String value){
            this.value = value;
            this.code = code;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }
    }


}
