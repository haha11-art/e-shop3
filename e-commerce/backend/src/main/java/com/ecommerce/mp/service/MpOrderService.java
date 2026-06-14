package com.ecommerce.mp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ecommerce.mp.entity.MpOrder;

/**
 * 订单 Service 接口
 */
public interface MpOrderService extends IService<MpOrder> {

    /**
     * 创建订单
     */
    MpOrder createOrder(MpOrder order);

    /**
     * 根据ID查询订单（含订单项）
     */
    MpOrder getOrderWithItems(Long orderId);

    /**
     * 根据用户ID分页查询订单
     */
    com.baomidou.mybatisplus.extension.plugins.pagination.Page<MpOrder> getOrdersByUserId(
            Long userId, String status, int page, int size);

    /**
     * 更新订单状态
     */
    boolean updateOrderStatus(Long orderId, String newStatus);

    /**
     * 删除订单
     */
    boolean deleteOrder(Long orderId);
}
