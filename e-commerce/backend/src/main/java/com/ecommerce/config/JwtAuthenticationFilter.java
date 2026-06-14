package com.ecommerce.config;

import com.ecommerce.common.Result;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * JWT认证拦截器 - 验证请求中的Token并提取用户信息
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        // 获取请求头中的Authorization
        String authorization = request.getHeader("Authorization");
        
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7); // 去掉"Bearer "前缀
            
            try {
                // 解析Token
                SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
                Claims claims = Jwts.parser()
                        .verifyWith(key)
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();
                
                // 提取用户信息并存入请求属性
                String userId = claims.getSubject();
                String username = claims.get("username", String.class);
                String role = claims.get("role", String.class);
                
                request.setAttribute("userId", Long.parseLong(userId));
                request.setAttribute("username", username);
                request.setAttribute("role", role);
                
                // Token有效，继续执行后续过滤器
                filterChain.doFilter(request, response);
                
            } catch (Exception e) {
                // Token无效或过期
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(
                    new com.fasterxml.jackson.databind.ObjectMapper()
                        .writeValueAsString(Result.error(401, "Token无效或已过期"))
                );
                return;
            }
        } else {
            // 没有Token，直接放行（某些接口不需要登录）
            filterChain.doFilter(request, response);
        }
    }
}
