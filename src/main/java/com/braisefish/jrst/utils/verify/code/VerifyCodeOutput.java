package com.braisefish.jrst.utils.verify.code;


public class VerifyCodeOutput {
    public VerifyCodeOutput(String uid, String base64){
        this.uid = uid;
        this.base64 = base64;
    }
    private String uid;
    private String base64;


    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getBase64() {
        return base64;
    }

    public void setBase64(String base64) {
        this.base64 = base64;
    }
}
