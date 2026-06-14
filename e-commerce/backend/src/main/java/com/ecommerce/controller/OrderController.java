package com.ecommerce.controller;

import com.ecommerce.common.Result;
import com.ecommerce.service.OrderService;
import com.ecommerce.util.UserContext;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 订单控制器 - 创建/查询/支付/取消/确认收货
 */
@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 创建订单
     * POST /api/orders
     * Body: { "shippingAddress": "...", "receiverName": "...", "receiverPhone": "...", "remark": "..." }
     */
    @PostMapping
    public Result<?> createOrder(@RequestBody(required = false) Map<String, String> body) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        String shippingAddress = body != null ? body.get("shippingAddress") : null;
        String receiverName = body != null ? body.get("receiverName") : null;
        String receiverPhone = body != null ? body.get("receiverPhone") : null;
        String remark = body != null ? body.get("remark") : null;
        return orderService.createOrder(userId, shippingAddress, receiverName, receiverPhone, remark);
    }

    /**
     * 获取用户订单列表
     * GET /api/orders?status=PENDING&page=0&size=10
     */
    @GetMapping
    public Result<?> getOrderList(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        return orderService.getOrderList(userId, status, page, size);
    }

    /**
     * 获取订单详情
     * GET /api/orders/{orderId}
     */
    @GetMapping("/{orderId}")
    public Result<?> getOrderDetail(@PathVariable Long orderId) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        return orderService.getOrderDetail(userId, orderId);
    }

    /**
     * 支付订单
     * PUT /api/orders/{orderId}/pay
     * Body: { "payType": "ALIPAY" }
     */
    @PutMapping("/{orderId}/pay")
    public Result<?> payOrder(
            @PathVariable Long orderId,
            @RequestBody Map<String, String> body) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        String payType = body.getOrDefault("payType", "ALIPAY");
        return orderService.payOrder(userId, orderId, payType);
    }

    /**
     * 取消订单
     * PUT /api/orders/{orderId}/cancel
     */
    @PutMapping("/{orderId}/cancel")
    public Result<?> cancelOrder(@PathVariable Long orderId) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        return orderService.cancelOrder(userId, orderId);
    }

    /**
     * 确认收货
     * PUT /api/orders/{orderId}/confirm
     */
    @PutMapping("/{orderId}/confirm")
    public Result<?> confirmReceive(@PathVariable Long orderId) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        return orderService.confirmReceive(userId, orderId);
    }

    /**
     * 管理员-获取所有订单
     * GET /api/orders/admin/all?page=0&size=10
     */
    @GetMapping("/admin/all")
    public Result<?> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (!UserContext.isAdmin()) {
            return Result.error(403, "无权限访问");
        }
        return orderService.getAllOrders(page, size);
    }

    /**
     * 管理员-发货
     * PUT /api/orders/admin/{orderId}/ship
     */
    @PutMapping("/admin/{orderId}/ship")
    public Result<?> shipOrder(@PathVariable Long orderId) {
        if (!UserContext.isAdmin()) {
            return Result.error(403, "无权限访问");
        }
        return orderService.shipOrder(orderId);
    }
}
