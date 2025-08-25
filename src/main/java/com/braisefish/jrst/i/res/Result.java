package com.braisefish.jrst.i.res;


import com.braisefish.jrst.i.JsonEntity;

public abstract class Result<T> implements JsonEntity {
    protected static final String SUCCESS_MSG = "成功";
    public static final int SUCCESS = 0;
    public static final int FAIL = -1;

    private int code;
    private String message;
    private T data;
    private long timestamp;

    public Result() {
        super();
        this.code = SUCCESS;
        this.message = SUCCESS_MSG;
        this.timestamp = System.currentTimeMillis();
    }

    public Result(T data) {
        this();
        this.data = data;
    }

    public Result(Throwable throwable) {
        this.code = FAIL;
        this.message = throwable.getMessage();
    }

    public abstract Result<T> success(T data);

    public abstract  Result<T> success();

    /**
     * 错误处理
     * @param msg
     * @return
     */
    public abstract Result<T> err(String msg);

    /**
     * 错误处理
     * @param code
     * @param msg
     * @return
     */
    public abstract Result<T> err(int code, String msg);


    public static <T> Result<T> ok(T data) {
        return new JResult<T>(data);
    }

    public static <R> Result<R> ok() {
        return new JResult<R>();
    }

    public static <R> Result<R> error(String msg) {
        var rest = new JResult<R>();
        rest.err(msg);
        return rest;
    }

    public static <R>  Result<R> error(int code, String msg) {
        var rest = new JResult<R>();
        rest.err(code, msg);
        return rest;
    }


    public int getCode() {
        return code;
    }

    public Result<T> setCode(int code) {
        this.code = code;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public Result<T> setMessage(String message) {
        this.message = message;
        return this;
    }

    public T getData() {
        return data;
    }

    public Result<T> setData(T data) {
        this.data = data;
        return this;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Result<T> setTimestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }
}
