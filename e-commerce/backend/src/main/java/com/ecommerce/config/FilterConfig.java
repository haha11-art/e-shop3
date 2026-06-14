package com.ecommerce.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 过滤器配置 - 注册JWT认证拦截器
 */
@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilter(JwtAuthenticationFilter filter) {
        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(filter);
        
        // 设置需要拦截的路径（除了登录注册等公开接口）
        registration.addUrlPatterns("/api/*");
        
        // 排除不需要Token验证的接口
        registration.addInitParameter("exclusions", "/api/users/login,/api/users/register,/api/products*,/api/products/categories*");
        
        registration.setName("jwtAuthenticationFilter");
        registration.setOrder(1); // 优先级
        
        return registration;
    }
}
