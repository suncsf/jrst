package com.braisefish.jrst.i;


import com.braisefish.jrst.utils.JsonUtils;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface JsonEntity extends IEntity {

    default String toJson() throws JsonProcessingException {
        return JsonUtils.toJson(this);
    }
}
