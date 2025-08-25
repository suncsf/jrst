package com.braisefish.jrst.i.page;


public class PageIn extends QueryIn {
    private Long current;
    private Integer size;


    public Long getCurrent() {
        return current;
    }

    public PageIn setCurrent(Long current) {
        this.current = current;
        return this;
    }

    public Integer getSize() {
        return size;
    }

    public PageIn setSize(Integer size) {
        this.size = size;
        return this;
    }
}
