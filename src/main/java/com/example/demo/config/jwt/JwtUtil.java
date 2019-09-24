package com.example.demo.config.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.example.demo.constant.WebConstant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
@Component
public class JwtUtil {


    private static String expireTime;


    private static String key;

    @Value("${expireTime}")
    private void setExpireTime(String expireTime) {
        JwtUtil.expireTime = expireTime;
    }

    @Value("${key}")
    private void setKey(String key) {
        JwtUtil.key = key;
    }

    /**
     * 创建token，里面放的是用户名
     *
     * @param account
     * @param currentTimeMills
     * @return
     */
    public static String createToken(String account, String currentTimeMills) {
        System.out.println(key);
        System.out.println(expireTime);
        return JWT.create()
                .withClaim(WebConstant.USER_ACCOUNT, account)
                .withClaim(WebConstant.CURRENT_TIME_MILLS, currentTimeMills)
                .withExpiresAt(new Date(System.currentTimeMillis() + Long.parseLong(expireTime)*1000))
                .sign(Algorithm.HMAC256(key));
    }

    /**
     * 从token中获取claim
     *
     * @param token
     * @return
     */
    public static String getClaim(String token, String claim) {
        return JWT.decode(token).getClaim(claim).asString();
    }

    /**
     * 校验token
     *
     * @param token
     * @return
     */
    public static boolean checkToken(String token) {
        try {
            JWT.require(Algorithm.HMAC256(key)).build().verify(token);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

}
