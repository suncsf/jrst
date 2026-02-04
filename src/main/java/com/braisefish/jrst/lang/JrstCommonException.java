package com.braisefish.jrst.lang;

public class JrstCommonException extends Exception {
    private Integer code = 0;

    public JrstCommonException() {
        super("通用异常抛出");
    }

    public JrstCommonException(String message) {
        super(message);
    }

    public JrstCommonException(Integer code, String message) {
        this(message);
        this.code = code;
    }

    public JrstCommonException(Throwable cause) {
        super(cause);
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }
}
