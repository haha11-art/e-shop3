package com.ecommerce.mp.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.common.Result;
import com.ecommerce.mp.entity.MpOrder;
import com.ecommerce.mp.service.MpOrderService;
import org.springframework.web.bind.annotation.*;

/**
 * 订单 CRUD 接口 (MyBatis Plus)
 */
@RestController
@RequestMapping("/api/mp/orders")
public class MpOrderController {

    private final MpOrderService mpOrderService;

    public MpOrderController(MpOrderService mpOrderService) {
        this.mpOrderService = mpOrderService;
    }

    /**
     * 创建订单
     * POST /api/mp/orders
     */
    @PostMapping
    public Result<MpOrder> createOrder(@RequestBody MpOrder order) {
        if (order.getUserId() == null) {
            return Result.error(400, "用户ID不能为空");
        }
        if (order.getTotalAmount() == null) {
            return Result.error(400, "订单总金额不能为空");
        }
        if (order.getPayAmount() == null) {
            return Result.error(400, "实付金额不能为空");
        }
        MpOrder created = mpOrderService.createOrder(order);
        return Result.success(created);
    }

    /**
     * 根据ID查询订单详情（含订单项）
     * GET /api/mp/orders/{id}
     */
    @GetMapping("/{id}")
    public Result<MpOrder> getOrderById(@PathVariable Long id) {
        MpOrder order = mpOrderService.getOrderWithItems(id);
        if (order == null) {
            return Result.error(404, "订单不存在");
        }
        return Result.success(order);
    }

    /**
     * 分页查询用户订单（可按状态筛选）
     * GET /api/mp/orders/user/{userId}?status=PAID&page=1&size=10
     */
    @GetMapping("/user/{userId}")
    public Result<Page<MpOrder>> getOrdersByUserId(
            @PathVariable Long userId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<MpOrder> orders = mpOrderService.getOrdersByUserId(userId, status, page, size);
        return Result.success(orders);
    }

    /**
     * 查询所有订单（分页）
     * GET /api/mp/orders?page=1&size=10
     */
    @GetMapping
    public Result<Page<MpOrder>> getAllOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<MpOrder> pageParam = new Page<>(page, size);
        Page<MpOrder> result = mpOrderService.page(pageParam);
        return Result.success(result);
    }

    /**
     * 更新订单状态
     * PUT /api/mp/orders/{id}/status?status=PAID
     */
    @PutMapping("/{id}/status")
    public Result<MpOrder> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        try {
            mpOrderService.updateOrderStatus(id, status);
            MpOrder order = mpOrderService.getOrderWithItems(id);
            return Result.success(order);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新订单信息
     * PUT /api/mp/orders/{id}
     */
    @PutMapping("/{id}")
    public Result<MpOrder> updateOrder(@PathVariable Long id, @RequestBody MpOrder order) {
        MpOrder existing = mpOrderService.getById(id);
        if (existing == null) {
            return Result.error(404, "订单不存在");
        }
        order.setId(id);
        mpOrderService.updateById(order);
        return Result.success(mpOrderService.getById(id));
    }

    /**
     * 删除订单
     * DELETE /api/mp/orders/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteOrder(@PathVariable Long id) {
        MpOrder existing = mpOrderService.getById(id);
        if (existing == null) {
            return Result.error(404, "订单不存在");
        }
        mpOrderService.deleteOrder(id);
        return Result.success();
    }
}
