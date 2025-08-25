package com.braisefish.jrst.utils.okhttp;

import com.braisefish.jrst.i.entity.KeyValuePair;
import com.braisefish.jrst.utils.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class OkHttpUtil {

    private KeyValuePair<String, String> basic;
    private Map<String, String> heads;

    private ObjectMapper objectMapper;
    private OkHttpClient client;
    private OkHttpClient.Builder okClient;


    private OkHttpUtil(Builder builder) {
        this.basic = builder.basic;
        this.heads = builder.heads;
//        this.method = builder.method;
        this.objectMapper = builder.objectMapper;
        if (Objects.isNull(this.objectMapper)) {
            this.objectMapper = JsonUtils.getObjectMapper();
        }

        okClient = new OkHttpClient.Builder()
                .addInterceptor(new LoggingInterceptor())
                .connectTimeout(Objects.isNull(builder.timeout)?5:builder.timeout, TimeUnit.MINUTES)
                .readTimeout(2, TimeUnit.MINUTES);

        if (this.basic != null) {
            client = okClient.addInterceptor(new BasicAuthInterceptor(basic.getKey(), basic.getValue()))
                    .build();
        } else {
            client = okClient.build();
        }
    }

    public String get(String url, Map<String, String> params) {
        try {
            Response response = getResponse(url, params);
            if (response != null) {
                return response.body().string();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String get(String url) {
        return get(url, null);
    }

    public Response getResponse(String url) throws IOException {
        return getResponse(url, null);
    }

    public Response getResponse(String url, Map<String, String> params) throws IOException {
        StringBuilder sb = new StringBuilder();
        if (params != null && params.size() > 0) {
            Set<Map.Entry<String, String>> entrySet = params.entrySet();
            sb.append("?");
            for (Map.Entry<String, String> entry : entrySet) {
                sb.append(entry.getKey());
                sb.append("=");
                try {
                    sb.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                sb.append("&");
            }
            sb.deleteCharAt(sb.length() - 1);
        }
        Request.Builder builder = new Request.Builder();
        if (heads != null && heads.size() > 0) {
            for (Map.Entry<String, String> item : heads.entrySet()) {
                builder = builder.addHeader(item.getKey(), item.getValue());
            }
        }
        Request request = builder
                .addHeader("Content-Type", "application/json")
                .url(url + sb.toString())
                .get()
                .build();
        Call call = client.newCall(request);
        return call.execute();
    }

    /**
     * POST 传输JSON请求
     *
     * @param url 请求地址
     * @param t   泛型对象
     * @param <T> 类型
     * @return content结果
     */
    public <T> String post(String url, T t) {
        try {
            Response response = postResponse(url, t);
            return response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public String post(String url) {
        return post(url, null);
    }

    public Response postResponse(String url) throws IOException {
        return postResponse(url, null);
    }

    /**
     * POST 传输JSON请求
     *
     * @param url 请求地址
     * @param t   泛型对象
     * @param <T> 类型
     * @return content结果
     */
    public <T> Response postResponse(String url, T t) throws IOException {
        Request.Builder builder = new Request.Builder();
        if (heads != null) {
            for (Map.Entry<String, String> item : heads.entrySet()) {
                builder = builder.addHeader(item.getKey(), item.getValue());
            }
        }
        Request.Builder requestBuilder = builder
                .url(url);
        Request request = null;
        if (Objects.nonNull(t)) {
            RequestBody requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8")
                    , objectMapper.writeValueAsBytes(t));
            request = requestBuilder.post(requestBody)
                    .build();
        } else {
            request = requestBuilder.build();
        }
        Call call = client.newCall(request);
        return call.execute();
    }

    public static class Builder {
        //        private Method method;
        private KeyValuePair basic;
        private ObjectMapper objectMapper;
        private Map<String, String> heads;
        private Integer timeout;
        public Builder setBasicAccount(KeyValuePair basic) {
            this.basic = basic;
            return this;
        }

        public Builder setObjectMapper(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
            return this;
        }

        public Builder setHeads(Map<String, String> heads) {
            this.heads = heads;
            return this;
        }
        public Builder setTimeout(Integer timeout) {
            this.timeout = timeout;
            return this;
        }

        public OkHttpUtil build() {
            return new OkHttpUtil(this);
        }
    }

}
