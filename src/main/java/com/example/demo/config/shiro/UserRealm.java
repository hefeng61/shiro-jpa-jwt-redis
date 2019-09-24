package com.example.demo.config.shiro;

import com.example.demo.config.jwt.JwtUtil;
import com.example.demo.config.redis.RedisUtil;
import com.example.demo.constant.WebConstant;
import com.example.demo.domain.JwtToken;
import com.example.demo.domain.Permission;
import com.example.demo.domain.Role;
import com.example.demo.domain.User;
import com.example.demo.mapper.PermissionMapper;
import com.example.demo.mapper.RoleMapper;
import com.example.demo.mapper.UserMapper;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 重写shiro的安全域，配置验证授权功能
 *
 * @author HF
 */
@Component
public class UserRealm extends AuthorizingRealm {

    @Autowired
    UserMapper userMapper;

    @Autowired
    RoleMapper roleMapper;

    @Autowired
    PermissionMapper permissionMapper;

    @Autowired
    RedisUtil redisUtil;

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof JwtToken;
    }

    /**
     * 根据用户名获取用户的角色权限信息，封装到SimpleAuthorizationInfo
     * 重写的缓存管理在这里使用，doGetAuthorizationInfo中调用了cache.put()方法，将权限角色信息都放入了redis中
     * <p>
     * <p>
     * <p>
     * 这步授权，在有@RequiresRoles或@RequiresPermissions时执行，执行时会先去redis里面查找有无对应的权限角色信息，
     * 如果没有，则去数据库查询，再放入redis中，后面再进来则会去去redis中的信息，减少了频繁读取数据库的压力
     *
     * @param principals
     * @return
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        String account = JwtUtil.getClaim(principals.toString(), WebConstant.USER_ACCOUNT);
        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
        List<Role> roleList = roleMapper.findByAccount(account);
        for (Role role : roleList) {
            authorizationInfo.addRole(role.getName());
            List<Permission> permissionList = permissionMapper.findByRoleName(role.getName());
            for (Permission permission : permissionList) {
                authorizationInfo.addStringPermission(permission.getName());
            }
        }

        return authorizationInfo;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        String tk = (String) token.getCredentials();
        String account = JwtUtil.getClaim(tk, WebConstant.USER_ACCOUNT);

        User user = userMapper.findByAccount(account);
        if (user == null) {
            throw new AuthenticationException("用户不存在");
        }
        System.out.println(JwtUtil.checkToken(tk));
        System.out.println(redisUtil.hasKey(WebConstant.REFRESH_TOKEN + account));
        if (JwtUtil.checkToken(tk) && redisUtil.hasKey(WebConstant.REFRESH_TOKEN + account)) {
            System.out.println("================" + WebConstant.REFRESH_TOKEN + account);
            String timeMills = redisUtil.get(WebConstant.REFRESH_TOKEN + account).toString();
            if (timeMills.equals(JwtUtil.getClaim(tk, WebConstant.CURRENT_TIME_MILLS))) {
                return new SimpleAuthenticationInfo(tk, tk, getName());
            }
        }
        throw new AuthenticationException("token过期或不正确");
    }
}
