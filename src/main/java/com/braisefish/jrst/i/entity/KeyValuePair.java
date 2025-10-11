package com.braisefish.jrst.i.entity;


import com.braisefish.jrst.i.JsonEntity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 键值
 *
 * @author sunchao
 * @date：2023/8/30 14:22
 * @since 1.0
 */
public class KeyValuePair<K, V> implements Comparable<KeyValuePair<K, V>>, Serializable, JsonEntity {

    private K key;
    private V value;
    private DoggyBox<?> doggyBox;

    private Double sort;
    /**
     * 创建一个只读实例
     */
    public KeyValuePair<K, V> unmodifiable() {
        return new KeyValuePair<K, V>(this) {
            @Override
            public KeyValuePair<K, V> setKey(K key) {
                throw new UnsupportedOperationException();
            }

            @Override
            public KeyValuePair<K, V> setValue(V value) {
                throw new UnsupportedOperationException();
            }
        };
    }

    public KeyValuePair() {
    }

    public KeyValuePair(KeyValuePair<K,V > kv) {
        this(kv.getKey(), kv.getValue(), kv.getSort(), kv.getDoggyBox());
    }
    public KeyValuePair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public KeyValuePair(K key, V value, Double sort) {
        this(key, value, sort, null);
    }

    public KeyValuePair(K key, V value, DoggyBox<?> doggyBox) {
        this(key, value, null, doggyBox);
    }

    public KeyValuePair(K key, V value, Double sort, DoggyBox<?> doggyBox) {
        this(key, value);
        this.sort = sort;
        this.doggyBox = doggyBox;
    }

    public Map<String, Object> getProperty() {
        if (Objects.isNull(this.getDoggyBox()) || Objects.isNull(this.getDoggyBox().getProperty())) {
            return new HashMap<>();
        }
        return this.getDoggyBox().getProperty();
    }

    public <T> T getData() {
        if (Objects.isNull(this.getDoggyBox())) {
            return null;
        }
        return (T) this.getDoggyBox().getData();
    }


    @Override
    public int compareTo(KeyValuePair<K, V> o) {
        if (Objects.isNull(o)) {
            return 0;
        }
        if (Objects.isNull(this.getSort()) || Objects.isNull(o.getSort())) {
            return 0;
        }
        return (int) (this.getSort() - o.getSort());
    }


    public K getKey() {
        return key;
    }

    public KeyValuePair<K, V> setKey(K key) {
        this.key = key;
        return this;
    }

    public V getValue() {
        return value;
    }

    public KeyValuePair<K, V> setValue(V value) {
        this.value = value;
        return this;
    }

    public KeyValuePair<K, V> setDoggyBox(DoggyBox<?> doggyBox) {
        this.doggyBox = doggyBox;
        return this;
    }

    public Double getSort() {
        return sort;
    }

    public KeyValuePair<K, V> setSort(Double sort) {
        this.sort = sort;
        return this;
    }


    public DoggyBox<?> getDoggyBox() {
        if (Objects.isNull(this.doggyBox)) {
            this.doggyBox = new DoggyBox<>();
        }
        return this.doggyBox;
    }
}
