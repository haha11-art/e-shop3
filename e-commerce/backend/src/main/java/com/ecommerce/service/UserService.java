package com.ecommerce.service;

import com.ecommerce.common.Result;
import com.ecommerce.entity.User;
import com.ecommerce.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户服务 - 注册、登录、JWT认证
 */
@Service
public class UserService {

    private final UserRepository userRepository;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 用户注册
     */
    public Result<?> register(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            return Result.error("用户名已存在");
        }
        user.setNickname(user.getNickname() != null ? user.getNickname() : user.getUsername());
        userRepository.save(user);
        return Result.success("注册成功", null);
    }

    /**
     * 用户登录
     */
    public Result<?> login(String username, String password) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null || !user.getPassword().equals(password)) {
            return Result.error("用户名或密码错误");
        }
        String token = generateToken(user);
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", sanitizeUser(user));
        return Result.success("登录成功", data);
    }

    /**
     * 获取用户信息
     */
    public Result<?> getUserInfo(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return Result.error("用户不存在");
        }
        return Result.success(sanitizeUser(user));
    }

    /**
     * 更新用户信息
     */
    public Result<?> updateUser(Long userId, User updatedUser) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return Result.error("用户不存在");
        }
        if (updatedUser.getNickname() != null) user.setNickname(updatedUser.getNickname());
        if (updatedUser.getEmail() != null) user.setEmail(updatedUser.getEmail());
        if (updatedUser.getPhone() != null) user.setPhone(updatedUser.getPhone());
        if (updatedUser.getAddress() != null) user.setAddress(updatedUser.getAddress());
        userRepository.save(user);
        return Result.success("更新成功", sanitizeUser(user));
    }

    /**
     * 生成JWT Token
     */
    private String generateToken(User user) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("username", user.getUsername())
                .claim("role", user.getRole())
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plusMillis(jwtExpiration)))
                .signWith(key)
                .compact();
    }

    /**
     * 解析Token获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            String subject = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
            return Long.parseLong(subject);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 过滤敏感信息
     */
    private Map<String, Object> sanitizeUser(User user) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", user.getId());
        map.put("username", user.getUsername());
        map.put("nickname", user.getNickname());
        map.put("email", user.getEmail());
        map.put("phone", user.getPhone());
        map.put("address", user.getAddress());
        map.put("role", user.getRole());
        return map;
    }
}
