package com.braisefish.jrst.utils.jwt;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.braisefish.jrst.lang.JwtAuthorazationException;

import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * JWT工具类
 * 提供与JWT（JSON Web Token）相关的实用方法，如生成和验证token
 *
 * @author 32365
 */
public class JwtUtils {
    private static String secretKey = "";

    /**
     *
     * @param key
     * @throws JwtAuthorazationException
     */
    public static void initJwtKey(String key) throws JwtAuthorazationException {
        if (StrUtil.isNotBlank(secretKey)) {
            throw new JwtAuthorazationException("已存在secretKey");
        }
        if(StrUtil.isBlank(key)){
            throw new JwtAuthorazationException("key不可为空值");
        }
        secretKey = key;
    }

    /**
     * 2小时
     */
    public final static int TOKEN_EXP_HOUR = 2;

    /**
     * 将给定的秘密字符串与一个固定的键值拼接起来.
     * <p>
     * 此方法的目的是为了构造一个新的字符串，它由输入的秘密字符串和一个预定义的键值组成.
     * 这种做法通常用于生成唯一的标识符或密钥，以便在系统中安全地使用或传输.
     *
     * @param secret 输入的秘密字符串，将与键值拼接.
     * @return 拼接了固定键值后的秘密字符串.
     */
    private static String getSecret(String secret) {
        return secret + secretKey;
    }

    /**
     * 创建JWT令牌
     *
     * @param jwtEntity 包含JWT令牌相关信息的实体对象
     * @return 生成的JWT令牌字符串
     */
    public static String create(JwtEntity jwtEntity) {
        Date dt = new Date();
        Date expiresDate = DateUtil.offsetHour(dt, ObjectUtil.isNull(jwtEntity.getTokenExpHour()) ? TOKEN_EXP_HOUR : jwtEntity.getTokenExpHour());
        var builder = JWT.create().withAudience(jwtEntity.getKey())
                .withIssuedAt(dt)
                .withExpiresAt(expiresDate);
        if (Objects.nonNull(jwtEntity.getClaimMap()) && !jwtEntity.getClaimMap().entrySet().isEmpty()) {
            for (var claim : jwtEntity.getClaimMap().entrySet()) {
                if (claim.getValue() instanceof String) {
                    builder.withClaim(claim.getKey(), (String) claim.getValue());
                } else if (claim.getValue() instanceof List<?>) {
                    builder.withClaim(claim.getKey(), (List<?>) claim.getValue());
                } else if (claim.getValue() instanceof Integer) {
                    builder.withClaim(claim.getKey(), (Integer) claim.getValue());
                } else if (claim.getValue() instanceof Long) {
                    builder.withClaim(claim.getKey(), (Long) claim.getValue());
                } else if (claim.getValue() instanceof Boolean) {
                    builder.withClaim(claim.getKey(), (Boolean) claim.getValue());
                } else if (claim.getValue() instanceof Double) {
                    builder.withClaim(claim.getKey(), (Double) claim.getValue());
                } else if (claim.getValue() instanceof Date) {
                    builder.withClaim(claim.getKey(), (Date) claim.getValue());
                }

            }
        }
        return builder.sign(Algorithm.HMAC256(getSecret(jwtEntity.getKey())));
    }


    /**
     * 检验token的合法性
     *
     * @param token  待验证的token字符串
     * @param secret 用于验证token的密钥，此处应传入用户的ID
     * @throws JwtAuthorazationException 如果token验证失败，则抛出此自定义异常
     */
    public static void verifyToken(String token, String secret) throws JwtAuthorazationException {
        DecodedJWT jwt = null;
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret + secretKey)).build();
            jwt = verifier.verify(token);
        } catch (Exception e) {
            throw new JwtAuthorazationException("token已失效~");
        }
    }

    /**
     * 获取签发对象
     * 解析JWT字符串并提取受众信息
     *
     * @param token JWT字符串，用于提取签发对象信息
     * @return 返回签发对象的标识字符串
     * @throws JwtAuthorazationException 当JWT字符串解析失败时抛出此异常
     */
    public static String getAudience(String token) throws JwtAuthorazationException {
        String audience = null;
        try {
            DecodedJWT decodedJWT = JWT.decode(token);
            audience = JWT.decode(token).getAudience().get(0);
        } catch (JWTDecodeException j) {
            //这里是token解析失败
            throw new JwtAuthorazationException("token解析失败");
        }
        return audience;
    }


    /**
     * 通过载荷名字获取载荷的值
     *
     * @param token JWT令牌，用于识别和解码JWT
     * @param name  载荷的名称，用于从JWT中提取特定的载荷
     * @return 返回指定名称的载荷对象，如果载荷不存在，则返回空的Claim对象
     */
    public static Claim getClaimByName(String token, String name) {
        return JWT.decode(token).getClaim(name);
    }
}
