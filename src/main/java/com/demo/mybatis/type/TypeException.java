package com.demo.mybatis.type;

/*
 * @Author: yinchao
 * @Date: 2025-05-21 18:16:00
 * @LastEditors: yinchao
 * @LastEditTime: 2025-05-21 18:16:00
 * @Description: 类型异常
 */
public class TypeException extends RuntimeException {

    private static final long serialVersionUID = 8614420898975117130L;

    public TypeException() {
        super();
    }

    public TypeException(String message) {
        super(message);
    }

    public TypeException(String message, Throwable cause) {
        super(message, cause);
    }

    public TypeException(Throwable cause) {
        super(cause);
    }
}
