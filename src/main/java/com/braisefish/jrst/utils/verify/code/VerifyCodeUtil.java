package com.braisefish.jrst.utils.verify.code;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.CircleCaptcha;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.braisefish.jrst.lang.JrstCommonException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author sunchao
 */
public class VerifyCodeUtil {
    private final static Logger log = LoggerFactory.getLogger(VerifyCodeUtil.class);
    /**
     * 定时清理过期验证码定时器
     */
    private  static Timer clearExpireVerifyCodeTimer;

    /**
     * 验证码缓存
     */
    private final static Map<String,VerifyCodeCache> VERIFY_CODE_PROPERTY = new ConcurrentHashMap<>();
    public static void verifyCode(IVerifyCodeEntry verifyCodeEntry) throws JrstCommonException {
        if(StrUtil.isBlank(verifyCodeEntry.getVerifyCode())){
            throw new JrstCommonException("请输入验证码");
        }
        if(StrUtil.isBlank(verifyCodeEntry.getVerifyCodeKey())){
            throw new JrstCommonException("验证码信息缺失");
        }
        if(!VERIFY_CODE_PROPERTY.containsKey(verifyCodeEntry.getVerifyCodeKey())){
            throw new JrstCommonException("验证码失效~");
        }
        VerifyCodeCache verifyCodeCache = VERIFY_CODE_PROPERTY.get(verifyCodeEntry.getVerifyCodeKey());
        if(verifyCodeCache.isExpire()){
            throw new JrstCommonException("验证码已失效~");
        }
        if(!verifyCodeCache.equals(verifyCodeEntry.getVerifyCodeKey(),verifyCodeEntry.getVerifyCode())){
            throw new JrstCommonException("验证码错误~");
        }
    }

    public static VerifyCodeOutput createVerifyCode(VerifyCodeInput verifyCodeInput) {
        if(StrUtil.isNotBlank(verifyCodeInput.getVerifyCodeKey())){
            VERIFY_CODE_PROPERTY.remove(verifyCodeInput.getVerifyCodeKey());
        }
        CircleCaptcha captcha = CaptchaUtil.createCircleCaptcha(verifyCodeInput.getWidth(), verifyCodeInput.getHeight(), verifyCodeInput.getCodeCount(), verifyCodeInput.getCircleCount());
        final String code = captcha.getCode();
        final String key = UUID.randomUUID().toString();
        log.debug("验证码生成成功，验证码为：{}", code);
        VERIFY_CODE_PROPERTY.put(key, new VerifyCodeCache(key,code,5*60*1000));
        String base64 ="data:image/png;base64," + captcha.getImageBase64();
        var output = new VerifyCodeOutput(key, base64);
        output.setCode(code);
        return output;
    }

    /**
     * 清理过期验证码
     */
    public static void clearExpireVerifyCode(){
        VERIFY_CODE_PROPERTY.entrySet().removeIf(entry -> entry.getValue().isExpire());
    }
    /**
     * 注册一个自动清理过期验证码的定时
     */
    public static void registerClearExpireVerifyCodeTimer(){
        clearExpireVerifyCodeTimer = new Timer();
        clearExpireVerifyCodeTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                clearExpireVerifyCode();
            }
        }, 1000, 1000);
    }
    public static void unRegisterClearExpireVerifyCodeTimer(){
        clearExpireVerifyCodeTimer.cancel();
    }

    private static class VerifyCodeCache {
        private final String key;
        private final String value;
        private final Date createTime;
        private final Integer expireMillisecond;
        public VerifyCodeCache(String key, String value, int expireMillisecond) {
            this.key = key;
            this.value = value;
            this.expireMillisecond = expireMillisecond;
            this.createTime = new Date();
        }
        /**
         * 是否过期
         * @return true:过期 false:未过期
         */
        public boolean isExpire(){
            return DateUtil.between(createTime,new Date(), DateUnit.MS) > expireMillisecond;
        }
        /**
         * 验证码是否正确
         * @param verifyCodeKey 验证码key
         * @param code 验证码
         * @return true:正确 false:错误
         */
        public boolean equals(String verifyCodeKey,String code) {
            return Objects.equals(key,verifyCodeKey) && Objects.equals(value,code);
        }

    }
}
