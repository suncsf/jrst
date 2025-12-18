package com.braisefish.jrst.utils.okhttp;

import cn.hutool.core.util.StrUtil;
import com.braisefish.jrst.i.entity.KeyValuePair;
import com.braisefish.jrst.utils.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author 32365
 */
public class OkHttpUtil {
    private static final Logger log = LoggerFactory.getLogger(OkHttpUtil.class);

    private final Map<String, String> heads;
    private final String baseUrl;

    private final ObjectMapper objectMapper;

    private OkHttpClient client;


    private OkHttpUtil(Builder builder) {
        //    private Method method;
        KeyValuePair<String, String> basic = builder.basic;
        this.heads = builder.heads;
        this.baseUrl = builder.baseUrl;
        if (Objects.isNull(builder.objectMapper)) {
            this.objectMapper = JsonUtils.getObjectMapper();
        } else {
            this.objectMapper = builder.objectMapper;
        }
        try {
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[]{};
                        }
                    }
            };

            OkHttpClient.Builder okClient = new OkHttpClient.Builder()
                    .connectTimeout(2, TimeUnit.MINUTES)
                    .readTimeout(2, TimeUnit.MINUTES);
            if (StrUtil.isNotBlank(this.baseUrl) && this.baseUrl.startsWith("https://")) {
                final SSLContext sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
                final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
                okClient = okClient.connectionSpecs(Collections.singletonList(ConnectionSpec.COMPATIBLE_TLS))
                        .sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0])
                        .hostnameVerifier((hostname, session) -> true);
                okClient.setHostnameVerifier$okhttp((hostname, session) -> true);
            }
            if (basic != null) {
                client = okClient.addInterceptor(new BasicAuthInterceptor(basic.getKey(), basic.getValue()))
                        .build();
            } else {
                client = okClient.build();
            }
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            log.error("init okhttp error", e);
        }


    }

    public String get(String url, Map<String, String> params) {
        try {
            Response response = getResponse(url, params);
            if (response != null) {
                if (response.body() != null) {
                    return response.body().string();
                }
            }
        } catch (IOException e) {
            log.error("get error", e);
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
        if (params != null && !params.isEmpty()) {
            Set<Map.Entry<String, String>> entrySet = params.entrySet();
            sb.append("?");
            for (Map.Entry<String, String> entry : entrySet) {
                sb.append(entry.getKey());
                sb.append("=");
                sb.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
                sb.append("&");
            }
            sb.deleteCharAt(sb.length() - 1);
        }
        Request.Builder builder = new Request.Builder();
        if (heads != null && !heads.isEmpty()) {
            for (Map.Entry<String, String> item : heads.entrySet()) {
                builder = builder.addHeader(item.getKey(), item.getValue());
            }
        }
        Request request = builder
                .addHeader("Content-Type", "application/json")
                .url((StrUtil.isBlank(baseUrl) ? "" : baseUrl) + url + sb)
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
        try (Response response = postResponse(url, t)) {
            if (response.body() != null) {
                return response.body().string();
            }
        } catch (IOException e) {
            log.error("post error", e);
        }
        return null;
    }
    /**
     * POST 传输JSON请求
     *
     * @param url 请求地址
     * @param t   泛型对象
     * @param <T> 类型
     * @return content结果
     */
    public <T> String post(String url, T t, Map<String, String> params) {
        try (Response response = postResponse(url, t, params)) {
            if (response.body() != null) {
                return response.body().string();
            }
        } catch (IOException e) {
            log.error("post error", e);
        }
        return null;
    }
    /**
     * POST 默认请求
     *
     * @param url 请求地址
     * @return content结果
     */
    public String post(String url) {
        return post(url, null);
    }

    /**
     * POST 默认请求
     *
     * @param url 请求地址
     * @return content结果
     */
    public Response postResponse(String url) throws IOException {
        return postResponse(url, null,null);
    }

    /**
     * POST 默认请求
     *
     * @param url 请求地址
     * @param data body参数
     * @return content结果
     */
    public <T> Response postResponse(String url, T data) throws IOException {
        return postResponse(url, data,null);
    }

    /**
     * POST 传输JSON请求
     *
     * @param url 请求地址
     * @param t   泛型对象
     * @param <T> 类型
     * @return content结果
     */
    public <T> Response postResponse(String url, T t, Map<String, String> params) throws IOException {
        Request.Builder builder = new Request.Builder();
        if (heads != null) {
            for (Map.Entry<String, String> item : heads.entrySet()) {
                builder = builder.addHeader(item.getKey(), item.getValue());
            }
        }
        StringBuilder sb = new StringBuilder();
        if (params != null && !params.isEmpty()) {
            Set<Map.Entry<String, String>> entrySet = params.entrySet();
            sb.append("?");
            for (Map.Entry<String, String> entry : entrySet) {
                sb.append(entry.getKey());
                sb.append("=");
                sb.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
                sb.append("&");
            }
            sb.deleteCharAt(sb.length() - 1);
        }
        Request.Builder requestBuilder = builder
                .url((StrUtil.isBlank(baseUrl) ? "" : baseUrl) + url + sb);

        RequestBody requestBody;
        if (Objects.nonNull(t)) {
            requestBody = FormBody.create(objectMapper.writeValueAsString(t), MediaType.parse("application/json; charset=utf-8"));
        } else {
            requestBody = FormBody.create("{}", MediaType.parse("application/json; charset=utf-8"));
        }
        Request request = requestBuilder.post(requestBody)
                .build();
        Call call = client.newCall(request);
        return call.execute();
    }

    /**
     * 构建者模式
     */
    public static class Builder {

        private KeyValuePair<String, String> basic;
        private Map<String, String> heads;
        private String baseUrl;
        private Integer timeout;
        private ObjectMapper objectMapper;

        public Builder setObjectMapper(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
            return this;
        }

        public Builder setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public Builder setBasicAccount(KeyValuePair<String, String> basic) {
            this.basic = basic;
            return this;
        }

        public Builder setHeads(Map<String, String> heads) {
            this.heads = heads;
            return this;
        }

        public Integer getTimeout() {
            return timeout;
        }

        public Builder setTimeout(Integer timeout) {
            this.timeout = timeout;
            return this;
        }

        /**
         * 构建
         *
         * @return OkHttpUtil
         */
        public OkHttpUtil build() {
            return new OkHttpUtil(this);
        }
    }

}
