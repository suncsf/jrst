package com.braisefish.jrst.i.entity;

public class LabelValuePair<L,V> {
    private L label;
    private V value;

    public L getLabel() {
        return label;
    }

    public void setLabel(L label) {
        this.label = label;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }
}
