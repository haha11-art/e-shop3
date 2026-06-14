package com.ecommerce.mp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ecommerce.mp.entity.MpOrder;
import com.ecommerce.mp.entity.MpOrderItem;
import com.ecommerce.mp.mapper.OrderItemMapper;
import com.ecommerce.mp.mapper.OrderMapper;
import com.ecommerce.mp.service.MpOrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 订单 Service 实现类 - 基于 MyBatis Plus ServiceImpl
 */
@Service
public class MpOrderServiceImpl extends ServiceImpl<OrderMapper, MpOrder> implements MpOrderService {

    private final OrderItemMapper orderItemMapper;

    public MpOrderServiceImpl(OrderItemMapper orderItemMapper) {
        this.orderItemMapper = orderItemMapper;
    }

    /**
     * 创建订单 - 自动生成订单号
     */
    @Override
    @Transactional
    public MpOrder createOrder(MpOrder order) {
        // 生成唯一订单号: 时间戳 + 随机数
        String orderNo = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%06d", ThreadLocalRandom.current().nextInt(1000000));
        order.setOrderNo(orderNo);

        // 默认状态
        if (order.getStatus() == null) {
            order.setStatus("PENDING");
        }
        if (order.getFreight() == null) {
            order.setFreight(java.math.BigDecimal.ZERO);
        }

        // 保存订单
        save(order);

        // 保存订单项
        if (order.getOrderItems() != null) {
            for (MpOrderItem item : order.getOrderItems()) {
                item.setOrderId(order.getId());
                orderItemMapper.insert(item);
            }
        }
        return order;
    }

    /**
     * 查询订单详情（含订单项）
     */
    @Override
    public MpOrder getOrderWithItems(Long orderId) {
        MpOrder order = getById(orderId);
        if (order != null) {
            // 查询订单项
            LambdaQueryWrapper<MpOrderItem> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(MpOrderItem::getOrderId, orderId);
            List<MpOrderItem> items = orderItemMapper.selectList(wrapper);
            order.setOrderItems(items);
        }
        return order;
    }

    /**
     * 按用户ID分页查询订单
     */
    @Override
    public Page<MpOrder> getOrdersByUserId(Long userId, String status, int page, int size) {
        Page<MpOrder> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<MpOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MpOrder::getUserId, userId);
        if (status != null && !status.isEmpty()) {
            wrapper.eq(MpOrder::getStatus, status);
        }
        wrapper.orderByDesc(MpOrder::getCreateTime);
        return page(pageParam, wrapper);
    }

    /**
     * 更新订单状态
     */
    @Override
    @Transactional
    public boolean updateOrderStatus(Long orderId, String newStatus) {
        MpOrder order = getById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        order.setStatus(newStatus);
        // 记录状态变更时间
        LocalDateTime now = LocalDateTime.now();
        switch (newStatus) {
            case "PAID" -> order.setPayTime(now);
            case "SHIPPED" -> order.setShipTime(now);
            case "COMPLETED" -> order.setCompleteTime(now);
        }
        return updateById(order);
    }

    /**
     * 删除订单（同时删除订单项）
     */
    @Override
    @Transactional
    public boolean deleteOrder(Long orderId) {
        // 先删除订单项
        LambdaQueryWrapper<MpOrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MpOrderItem::getOrderId, orderId);
        orderItemMapper.delete(wrapper);
        // 再删除订单
        return removeById(orderId);
    }
}
