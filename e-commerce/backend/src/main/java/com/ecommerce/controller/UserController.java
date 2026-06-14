package com.ecommerce.controller;

import com.ecommerce.common.Result;
import com.ecommerce.entity.User;
import com.ecommerce.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户控制器 - 注册、登录、用户信息管理
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 用户注册
     * POST /api/users/register
     */
    @PostMapping("/register")
    public Result<?> register(@RequestBody User user) {
        return userService.register(user);
    }

    /**
     * 用户登录
     * POST /api/users/login
     */
    @PostMapping("/login")
    public Result<?> login(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");
        return userService.login(username, password);
    }

    /**
     * 获取用户信息
     * GET /api/users/{id}
     */
    @GetMapping("/{id}")
    public Result<?> getUserInfo(@PathVariable Long id) {
        return userService.getUserInfo(id);
    }

    /**
     * 更新用户信息
     * PUT /api/users/{id}
     */
    @PutMapping("/{id}")
    public Result<?> updateUser(@PathVariable Long id, @RequestBody User user) {
        return userService.updateUser(id, user);
    }
}
