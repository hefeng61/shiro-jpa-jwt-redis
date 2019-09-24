package com.example.demo.domain;

import org.apache.shiro.authc.AuthenticationToken;

/**
 * 用于将String类型的token转换成可以直接getSubject（）.login()使用的类型
 */
public class JwtToken implements AuthenticationToken {

    private String token;

    public JwtToken(String token) {
        this.token = token;
    }

    @Override
    public Object getPrincipal() {
        return token;
    }

    @Override
    public Object getCredentials() {
        return token;
    }
}
