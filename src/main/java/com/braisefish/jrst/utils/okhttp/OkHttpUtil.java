package com.braisefish.jrst.utils.okhttp;

import cn.hutool.core.util.StrUtil;
import com.braisefish.jrst.i.entity.KeyValuePair;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class OkHttpUtil {
    private static final Logger log = LoggerFactory.getLogger(OkHttpUtil.class);

    //    private Method method;
    private KeyValuePair<String, String> basic;
    private Map<String, String> heads;
    private String baseUrl;

    private static ObjectMapper objectMapper;

    private OkHttpClient client;
    private OkHttpClient.Builder okClient;

    static {
        objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private OkHttpUtil(Builder builder) {
        this.basic = builder.basic;
        this.heads = builder.heads;
        this.baseUrl = builder.baseUrl;
//        this.method = builder.method;
// Create a trust manager that does not validate certificate chains
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
            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            okClient = new OkHttpClient.Builder()
                    .connectionSpecs(Collections.singletonList(ConnectionSpec.COMPATIBLE_TLS))
                    .sslSocketFactory(sslSocketFactory,(X509TrustManager) trustAllCerts[0])
                    .hostnameVerifier((hostname, session) -> true)
                    .connectTimeout(2, TimeUnit.MINUTES)
                    .readTimeout(2, TimeUnit.MINUTES);
            okClient.setHostnameVerifier$okhttp((hostname, session) -> true);

            if (this.basic != null) {
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
                .url((StrUtil.isBlank(baseUrl) ? "" : baseUrl) + url + sb.toString())
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
                .url((StrUtil.isBlank(baseUrl) ? "" : baseUrl) + url);
        Request request = null;
        if (null != t) {
            RequestBody requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8")
                    , objectMapper.writeValueAsBytes(t));
            request = requestBuilder.post(requestBody)
                    .build();
        } else {
            RequestBody requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8"), "{}");
            request = requestBuilder.post(requestBody).build();

        }
        Call call = client.newCall(request);
        return call.execute();
    }

    public static class Builder {
        //        private Method method;
        private KeyValuePair<String, String> basic;
        private Map<String, String> heads;
        private String baseUrl;

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

        public OkHttpUtil build() {
            return new OkHttpUtil(this);
        }
    }

}
