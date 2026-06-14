package com.ecommerce.controller;

import com.ecommerce.common.Result;
import com.ecommerce.service.CartService;
import com.ecommerce.util.UserContext;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 购物车控制器 - 添加/修改/删除/查询购物车
 */
@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "*")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    /**
     * 添加商品到购物车
     * POST /api/cart
     * Body: { "productId": 1, "quantity": 1 }
     */
    @PostMapping
    public Result<?> addToCart(@RequestBody Map<String, Integer> body) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        Long productId = body.get("productId").longValue();
        Integer quantity = body.getOrDefault("quantity", 1);
        return cartService.addToCart(userId, productId, quantity);
    }

    /**
     * 获取购物车列表
     * GET /api/cart
     */
    @GetMapping
    public Result<?> getCartList() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        return cartService.getCartList(userId);
    }

    /**
     * 更新购物车商品数量
     * PUT /api/cart/{cartItemId}?quantity=2
     */
    @PutMapping("/{cartItemId}")
    public Result<?> updateQuantity(
            @PathVariable Long cartItemId,
            @RequestParam Integer quantity) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        return cartService.updateQuantity(userId, cartItemId, quantity);
    }

    /**
     * 删除购物车商品
     * DELETE /api/cart/{cartItemId}
     */
    @DeleteMapping("/{cartItemId}")
    public Result<?> removeFromCart(@PathVariable Long cartItemId) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        return cartService.removeFromCart(userId, cartItemId);
    }

    /**
     * 切换选中状态
     * PUT /api/cart/{cartItemId}/toggle
     */
    @PutMapping("/{cartItemId}/toggle")
    public Result<?> toggleSelected(@PathVariable Long cartItemId) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        return cartService.toggleSelected(userId, cartItemId);
    }

    /**
     * 清空购物车
     * DELETE /api/cart/clear
     */
    @DeleteMapping("/clear")
    public Result<?> clearCart() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        return cartService.clearCart(userId);
    }
}
