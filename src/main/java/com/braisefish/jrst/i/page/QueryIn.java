package com.braisefish.jrst.i.page;

import java.io.Serializable;

public class QueryIn implements Serializable {
    private String field;

    public String getField() {
        return field;
    }

    public QueryIn setField(String field) {
        this.field = field;
        return this;
    }
}
