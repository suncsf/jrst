package com.braisefish.jrst.lang;


public class JwtAuthorazationException extends AuthorizationException {
    public JwtAuthorazationException(){
        super();
    }
    public JwtAuthorazationException(String message) {
        super(message);
    }
}
