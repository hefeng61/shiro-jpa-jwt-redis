package com.example.demo.config.shiro;

import com.example.demo.config.cache.CustomCacheManager;
import com.example.demo.config.jwt.JwtFilter;
import com.example.demo.domain.User;
import org.apache.shiro.mgt.DefaultSessionStorageEvaluator;
import org.apache.shiro.mgt.DefaultSubjectDAO;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.redis.core.RedisTemplate;

import javax.servlet.Filter;
import java.util.HashMap;
import java.util.Map;

/**
 * shiro配置类
 */
@Configuration
//@ComponentScan(basePackages = "com.example.demo.config.redis")
public class ShiroConfig {

//    @Autowired
//    RedisTemplate<String,Object> redisTemplate;

    @Bean
    //,CustomCacheManager cacheManager
    public DefaultWebSecurityManager securityManager(RedisTemplate<String,Object> template,CustomCacheManager cacheManager) {
        DefaultWebSecurityManager webSecurityManager = new DefaultWebSecurityManager();
        webSecurityManager.setRealm(myRealm());
        //关闭自带的session存储，使用重写的cache接口
        DefaultSubjectDAO subjectDAO = new DefaultSubjectDAO();
        DefaultSessionStorageEvaluator sessionStorageEvaluator = new DefaultSessionStorageEvaluator();
        sessionStorageEvaluator.setSessionStorageEnabled(false);
        subjectDAO.setSessionStorageEvaluator(sessionStorageEvaluator);
        webSecurityManager.setSubjectDAO(subjectDAO);
//        webSecurityManager.setCacheManager(customCacheManager(template));
        webSecurityManager.setCacheManager(cacheManager);
        return webSecurityManager;
    }

    /**
     * 注意，这里有个大坑，如果此处的方法名跟自己定义的Realm一致，如userRealm，
     * 则会报错，提示相同姓名的realm已经引入过了
     *
     * @return
     */
    @Bean
    public UserRealm myRealm() {
        UserRealm userRealm = new UserRealm();
        return userRealm;
    }

    /**
     * 自定义shiro的过滤器，引入自定的jwtfilter，所有的请求/**都走此过滤器，
     * @param securityManager
     * @return
     */
    @Bean
    public ShiroFilterFactoryBean factoryBean(DefaultWebSecurityManager securityManager) {
        ShiroFilterFactoryBean bean = new ShiroFilterFactoryBean();
        bean.setSecurityManager(securityManager);
//        bean.setLoginUrl("/login");
        //自定义过滤器
        Map<String, Filter> filterMap = new HashMap<>();
        //这里有个问题，如果此处过滤器用的时@bean引入的，那么设置的login-anon还是会走realm验证
        //如果使用new方式创建的filter，则login-anon设置会生效，这里还不明白为何
        //但是用new方式创建后，会出现调两次realm的问题
//        filterMap.put("jwtFilter", new JwtFilter());
        filterMap.put("jwtFilter", jwtFilter());
        bean.setFilters(filterMap);
        //设置过滤的条件
        Map<String, String> filterRuleMap = new HashMap<>();
        filterRuleMap.put("/login/**", "anon");
        //这一步很关键，设置除了上面的路径之外所有的请求都要走jwtfilter
        filterRuleMap.put("/test", "jwtFilter");
        bean.setFilterChainDefinitionMap(filterRuleMap);
        return bean;
    }

    @Bean
    public JwtFilter jwtFilter() {
        return new JwtFilter();
    }

    /**
     * 开启shiro的AOP支持
     *
     * @return
     */
    @Bean
    @DependsOn("lifecycleBeanPostProcessor")
    public DefaultAdvisorAutoProxyCreator proxyCreator() {
        DefaultAdvisorAutoProxyCreator creator = new DefaultAdvisorAutoProxyCreator();
        creator.setProxyTargetClass(true);
        return creator;
    }

    @Bean
    public LifecycleBeanPostProcessor lifecycleBeanPostProcessor() {
        return new LifecycleBeanPostProcessor();
    }

    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(DefaultWebSecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor advisor = new AuthorizationAttributeSourceAdvisor();
        advisor.setSecurityManager(securityManager);
        return advisor;
    }


//    public CustomCacheManager customCacheManager(RedisTemplate redisTemplate) {
//        return new CustomCacheManager(redisTemplate);
//    }

//    public RedisTemplate<String, Object> redisTemplate() {
//        return new RedisTemplate<String, Object>();
//    }

}
