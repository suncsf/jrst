package com.braisefish.jrst.i.res;

public class JResult<T> extends Result<T> {

    public JResult() {
        super();
    }

    public JResult(T data) {
        super(data);
    }

    public JResult(Throwable throwable) {
        super((throwable));
    }
    @Override
    public  Result<T> success(T data) {
        setData(data);
        return this;
    }

    @Override
    public  Result<T> success() {
        this.setCode(SUCCESS);
        this.setMessage(SUCCESS_MSG);
        return this;
    }

    @Override
    public Result<T> err(String msg) {
        this.setCode(FAIL);
        this.setMessage(msg);
        return this;
    }

    @Override
    public Result<T> err(int code, String msg) {
        this.setCode(code);
        this.setMessage(msg);
        return this;
    }


}
