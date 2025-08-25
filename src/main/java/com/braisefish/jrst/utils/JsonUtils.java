package com.braisefish.jrst.utils;

import cn.hutool.core.util.ObjectUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.text.SimpleDateFormat;
import java.util.Map;

public class JsonUtils {
    private static ObjectMapper OBJECT_MAPPER;

    static {
        OBJECT_MAPPER = new ObjectMapper();
        //在反序列化时忽略在 json 中存在但 Java 对象不存在的属性
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        //在序列化时自定义时间日期格式
        OBJECT_MAPPER.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
    }


    public static String toJson(Object value) throws JsonProcessingException {
        return OBJECT_MAPPER.writeValueAsString(value);
    }

    public static <K,V>  Map<K,V>  convertMap(Object value) throws JsonProcessingException {
        var json = toJson(value);
        return OBJECT_MAPPER.readValue(json,new TypeReference<Map<K, V>>() {});
    }
    public static ObjectMapper getObjectMapper()  {
        return ObjectUtil.cloneByStream(OBJECT_MAPPER);
    }
}
