package com.example.demo.controller;

import com.example.demo.config.jwt.JwtUtil;
import com.example.demo.config.redis.RedisUtil;
import com.example.demo.constant.WebConstant;
import com.example.demo.domain.Result;
import com.example.demo.domain.User;
import com.example.demo.mapper.UserMapper;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@RestController
public class LoginController {
    @Autowired
    UserMapper userMapper;

    @Autowired
    RedisUtil redisUtil;

    /**
     * 登陆处理，如果用户名密码正确，则生成token，并将当前时间戳作为value放入redis，
     * refreshtoken用来验证用户是否长时间没有登陆，如果是，则提示token过期，并重新登陆
     *
     * @param user
     * @param response
     * @return
     */
    @RequestMapping("/login")
    public Result login(User user, HttpServletResponse response) {
        String account = user.getAccount();
        User user1 = userMapper.findByAccount(account);
        if (!user.getPassword().equals(user1.getPassword())) {
            return new Result("0", "用户名或密码错误");
        }
        String currentTimeMillis = String.valueOf(System.currentTimeMillis());
        String token = JwtUtil.createToken(account, currentTimeMillis);
        System.out.println("=================" + WebConstant.REFRESH_TOKEN + account);
        redisUtil.set(WebConstant.REFRESH_TOKEN + account, currentTimeMillis, 15 * 60 * 1000L);
//        redisUtil.set(WebConstant.REFRESH_TOKEN+account,currentTimeMillis);
//        redisUtil.setExpire(WebConstant.REFRESH_TOKEN+account,15*60*1000L);
        response.setHeader("Authtization", token);
        System.out.println("================登陆成功=============");
        return new Result("1", "登陆成功");
    }

    @RequestMapping("/test")
//    @RequiresRoles(logical = Logical.OR, value = {"admin"})
    @RequiresPermissions(value = {"curd"})
    public Result test() {
        return new Result("1", "ok");
    }
}
