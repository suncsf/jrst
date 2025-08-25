package com.braisefish.jrst.utils.jwt;


import com.braisefish.jrst.i.JsonEntity;

import java.util.HashMap;
import java.util.Map;

public class JwtEntity implements JsonEntity {
    private String key;
    private Integer tokenExpHour;
    private Map<String,?> claimMap;
    public JwtEntity(){
        this.claimMap = new HashMap<>();
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Integer getTokenExpHour() {
        return tokenExpHour;
    }

    public void setTokenExpHour(Integer tokenExpHour) {
        this.tokenExpHour = tokenExpHour;
    }

    public Map<String, ?> getClaimMap() {
        return claimMap;
    }

    public void setClaimMap(Map<String, ?> claimMap) {
        this.claimMap = claimMap;
    }
}
