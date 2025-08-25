package com.braisefish.jrst.i.entity;


import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author 32365
 */
public class DoggyBox<T> {
    public DoggyBox(){}
    public DoggyBox(T data, Map<String,Object> property){
        this.data = data;
        this.property = property;
    }

    public static <T> DoggyBox<T> of(T data){
        return new DoggyBox<T>(data,new HashMap<>());
    }
    public static <T> DoggyBox<T> of(T data,Map<String,Object> property){
        if(Objects.isNull(property)){
            property = new HashMap<>();
        }
        return new DoggyBox<T>(data,property);
    }

    private Map<String,Object> property;
    private T data;


    public Map<String, Object> getProperty() {
        return property;
    }

    public DoggyBox<T> setProperty(Map<String, Object> property) {
        this.property = property;
        return this;
    }

    public T getData() {
        return data;
    }

    public DoggyBox<T> setData(T data) {
        this.data = data;
        return this;
    }
}
