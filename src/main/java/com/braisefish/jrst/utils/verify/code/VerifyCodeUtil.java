package com.braisefish.jrst.utils.verify.code;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.CircleCaptcha;
import cn.hutool.core.util.StrUtil;
import com.braisefish.jrst.i.entity.KeyValuePair;
import com.braisefish.jrst.lang.JrstCommonException;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class VerifyCodeUtil {
  
    private final static Map<String,Object> VERIFY_CODE_PROPERTY = new ConcurrentHashMap<>();
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
        if(!verifyCodeEntry.getVerifyCode().equals(VERIFY_CODE_PROPERTY.get(verifyCodeEntry.getVerifyCodeKey()))){
            throw new JrstCommonException("验证码错误~");
        }
    }

    public static VerifyCodeOutput createVerifyCode(VerifyCodeInput verifyCodeInput) throws JrstCommonException {
        if(StrUtil.isNotBlank(verifyCodeInput.getVerifyCodeKey())){
            VERIFY_CODE_PROPERTY.remove(verifyCodeInput.getVerifyCodeKey());
        }
        CircleCaptcha captcha = CaptchaUtil.createCircleCaptcha(verifyCodeInput.getWidth(), verifyCodeInput.getHeight(), verifyCodeInput.getCodeCount(), verifyCodeInput.getCircleCount());
        final String code = captcha.getCode();
        final String key = UUID.randomUUID().toString();
        VERIFY_CODE_PROPERTY.put(key, code);
        String base64 ="data:image/png;base64," + captcha.getImageBase64();
        return new VerifyCodeOutput(key, base64);
    }
}
