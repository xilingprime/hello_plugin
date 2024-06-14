package com.xiling.hello_plugin.bean;

public class ReturnMessageBean {
    /**
     * 1.接受通话
     * 2.连接成功
     * 3.
     */
    String code;

    /**
     * 数据
     */
    String data;

    /**
     * 信息
     */
    String msg;

    // 构造方法
    public ReturnMessageBean(String code, String data, String msg) {
        this.code = code;
        this.data = data;
        this.msg = msg;
    }

    public String toJson() {
        return "{" +
                "\"code\":\"" + code + '\"' +
                ", \"data\":\"" + data + '\"' +
                ", \"msg\":\"" + msg + '\"' +
                '}';
    }
}
