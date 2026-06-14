package com.ecommerce.service;

import com.ecommerce.common.Result;
import com.ecommerce.entity.*;
import com.ecommerce.repository.CartItemRepository;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 订单服务 - 创建订单、查询订单、订单状态管理
 */
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public OrderService(OrderRepository orderRepository,
                        CartItemRepository cartItemRepository,
                        ProductRepository productRepository,
                        UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    /**
     * 创建订单（从购物车选中商品下单）
     */
    @Transactional
    public Result<?> createOrder(Long userId, String shippingAddress, String receiverName,
                                  String receiverPhone, String remark) {
        // 获取选中的购物车商品
        List<CartItem> selectedItems = cartItemRepository.findByUserIdAndSelectedTrue(userId);
        if (selectedItems.isEmpty()) {
            return Result.error("购物车中没有选中的商品");
        }

        User user = userRepository.findById(userId).orElseThrow();
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        // 检查库存并构建订单项
        for (CartItem cartItem : selectedItems) {
            Product product = cartItem.getProduct();
            if (product.getStock() < cartItem.getQuantity()) {
                return Result.error("商品 [" + product.getName() + "] 库存不足");
            }

            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);

            OrderItem orderItem = OrderItem.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .productImage(product.getImageUrl())
                    .unitPrice(product.getPrice())
                    .quantity(cartItem.getQuantity())
                    .totalPrice(itemTotal)
                    .build();
            orderItems.add(orderItem);
        }

        // 生成订单编号
        String orderNo = "EC" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", new Random().nextInt(10000));

        // 创建订单
        Order order = Order.builder()
                .orderNo(orderNo)
                .user(user)
                .totalAmount(totalAmount)
                .payAmount(totalAmount)
                .shippingAddress(shippingAddress != null ? shippingAddress : user.getAddress())
                .receiverName(receiverName != null ? receiverName : user.getNickname())
                .receiverPhone(receiverPhone != null ? receiverPhone : user.getPhone())
                .remark(remark)
                .status("PENDING")
                .build();

        orderRepository.save(order);

        // 关联订单项
        for (OrderItem item : orderItems) {
            item.setOrder(order);
        }
        order.setOrderItems(orderItems);
        orderRepository.save(order);

        // 扣减库存、增加销量
        for (CartItem cartItem : selectedItems) {
            Product product = cartItem.getProduct();
            product.setStock(product.getStock() - cartItem.getQuantity());
            product.setSalesCount(product.getSalesCount() + cartItem.getQuantity());
            productRepository.save(product);
        }

        // 清除已下单的购物车项
        cartItemRepository.deleteAll(selectedItems);

        return Result.success("下单成功", order);
    }

    /**
     * 获取用户订单列表
     */
    public Result<?> getOrderList(Long userId, String status, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Order> orderPage;

        if (status != null && !status.isEmpty()) {
            orderPage = orderRepository.findByUserIdAndStatusOrderByCreateTimeDesc(userId, status, pageRequest);
        } else {
            orderPage = orderRepository.findByUserIdOrderByCreateTimeDesc(userId, pageRequest);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("orders", orderPage.getContent());
        data.put("totalPages", orderPage.getTotalPages());
        data.put("totalElements", orderPage.getTotalElements());
        data.put("currentPage", page);
        return Result.success(data);
    }

    /**
     * 获取订单详情
     */
    public Result<?> getOrderDetail(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null || !order.getUser().getId().equals(userId)) {
            return Result.error("订单不存在");
        }
        return Result.success(order);
    }

    /**
     * 支付订单
     */
    @Transactional
    public Result<?> payOrder(Long userId, Long orderId, String payType) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null || !order.getUser().getId().equals(userId)) {
            return Result.error("订单不存在");
        }
        if (!"PENDING".equals(order.getStatus())) {
            return Result.error("订单状态不允许支付");
        }

        order.setStatus("PAID");
        order.setPayType(payType);
        order.setPayTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        orderRepository.save(order);

        return Result.success("支付成功", order);
    }

    /**
     * 取消订单
     */
    @Transactional
    public Result<?> cancelOrder(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null || !order.getUser().getId().equals(userId)) {
            return Result.error("订单不存在");
        }
        if (!"PENDING".equals(order.getStatus())) {
            return Result.error("只能取消待付款的订单");
        }

        // 恢复库存
        for (OrderItem item : order.getOrderItems()) {
            Product product = productRepository.findById(item.getProductId()).orElse(null);
            if (product != null) {
                product.setStock(product.getStock() + item.getQuantity());
                product.setSalesCount(product.getSalesCount() - item.getQuantity());
                productRepository.save(product);
            }
        }

        order.setStatus("CANCELLED");
        order.setUpdateTime(LocalDateTime.now());
        orderRepository.save(order);

        return Result.success("订单已取消", order);
    }

    /**
     * 确认收货
     */
    @Transactional
    public Result<?> confirmReceive(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null || !order.getUser().getId().equals(userId)) {
            return Result.error("订单不存在");
        }
        if (!"SHIPPED".equals(order.getStatus())) {
            return Result.error("订单状态不允许确认收货");
        }

        order.setStatus("COMPLETED");
        order.setCompleteTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        orderRepository.save(order);

        return Result.success("已确认收货", order);
    }

    /**
     * 管理员-所有订单列表
     */
    public Result<?> getAllOrders(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Order> orderPage = orderRepository.findAllByOrderByCreateTimeDesc(pageRequest);

        Map<String, Object> data = new HashMap<>();
        data.put("orders", orderPage.getContent());
        data.put("totalPages", orderPage.getTotalPages());
        data.put("totalElements", orderPage.getTotalElements());
        data.put("currentPage", page);
        return Result.success(data);
    }

    /**
     * 管理员-发货
     */
    @Transactional
    public Result<?> shipOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            return Result.error("订单不存在");
        }
        if (!"PAID".equals(order.getStatus())) {
            return Result.error("只能对待发货的订单进行发货");
        }

        order.setStatus("SHIPPED");
        order.setShipTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        orderRepository.save(order);

        return Result.success("发货成功", order);
    }
}
