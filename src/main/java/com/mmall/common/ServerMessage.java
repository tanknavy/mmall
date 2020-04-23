package com.mmall.common;

public enum  ServerMessage {

    SUCESS(1,"sucess"),
    FAIL(2,"failed");

    private final int code;
    private final String msg;

    ServerMessage(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
