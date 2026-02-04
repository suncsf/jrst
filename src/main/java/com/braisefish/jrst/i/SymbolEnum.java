package com.braisefish.jrst.i;

public enum SymbolEnum{
    eq("==="),
    gt(">"),
    lt("<"),

    and("&&"),
    or("||")
    ;
    private String value;
    SymbolEnum(String value){
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
