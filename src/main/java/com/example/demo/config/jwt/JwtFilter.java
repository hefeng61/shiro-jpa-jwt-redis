package com.example.demo.config.jwt;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.example.demo.config.redis.RedisUtil;
import com.example.demo.constant.WebConstant;
import com.example.demo.domain.JwtToken;
import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.SignatureException;

/**
 * jwt过滤器
 */
public class JwtFilter extends BasicHttpAuthenticationFilter {

    @Autowired
    RedisUtil redisUtil;

    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        if (isLoginAttempt(request, response)) {
            try {
                this.executeLogin(request, response);
            } catch (Exception e) {
                e.printStackTrace();
                String msg = e.getMessage();
                Throwable cause = e.getCause();
                if (cause != null && cause instanceof SignatureException) {
                    msg = "签名错误" + cause.getMessage();
                } else if (cause != null && cause instanceof TokenExpiredException) {
                    if (this.refrshToken(request, response)) {
                        return true;
                    } else {
                        msg = "token过期" + cause.getMessage();
                    }
                } else {
                    msg = cause.getMessage();
                }
            }
        }
        return true;
    }

    @Override
    protected boolean executeLogin(ServletRequest request, ServletResponse response) throws Exception {
        //从请求头中获取token，放入自定义的token类中
        JwtToken jwtToken = new JwtToken(getAuthzHeader(request));
        //在这步调用自定义的UserRealm，进行用户账号密码权限等验证
        this.getSubject(request, response).login(jwtToken);
        return true;
    }

    /**
     * 判断是否尝试登陆
     *
     * @param request
     * @param response
     * @return
     */
    @Override
    protected boolean isLoginAttempt(ServletRequest request, ServletResponse response) {
        return true;
    }

    /**
     * 判断refreshToken是否过期，如果过期，则提示重新登陆，重新生成accessToken并刷新refrshToken，
     * 否则刷新accessToken
     *
     * @param request
     * @param response
     * @return
     */
    public boolean refrshToken(ServletRequest request, ServletResponse response) {
        String token = getAuthzHeader(request);
        String account = JwtUtil.getClaim(token, WebConstant.USER_ACCOUNT);
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        //先判断refreshToken是否存在
        if (redisUtil.hasKey(WebConstant.REFRESH_TOKEN + account)) {
            //从refrshtoken中取出时间戳
            String current = (String) redisUtil.get(WebConstant.REFRESH_TOKEN + account);
            //与token中的时间戳一致，则重新生成token，并更新refreshToken为当前时间，并用新的token去登陆
            if (current.equals(JwtUtil.getClaim(token, WebConstant.CURRENT_TIME_MILLS))) {
                String currentTimeMills = String.valueOf(System.currentTimeMillis());
                String newToken = JwtUtil.createToken(account, currentTimeMills);
                redisUtil.set(WebConstant.REFRESH_TOKEN + account, currentTimeMills, 5 * 60 * 1000L);
                this.getSubject(request, response).login(new JwtToken(token));
                //生成新的token，放入header中，返回给前端

                httpServletResponse.setHeader("Authorization", token);
                httpServletResponse.setHeader("Access-Control-Expose-Headers", "Authorization");
                return true;
            } else {
                return false;
            }
        }
        //refreshToken不存在，则重定向到百度
        try {
            httpServletResponse.sendRedirect("www.baidu.com");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;

    }

}
