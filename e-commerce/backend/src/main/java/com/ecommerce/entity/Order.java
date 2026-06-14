package com.ecommerce.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单实体类
 */
@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 订单编号 */
    @Column(name = "order_no", unique = true, nullable = false, length = 32)
    private String orderNo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** 订单总金额 */
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    /** 实付金额 */
    @Column(name = "pay_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal payAmount;

    /** 运费 */
    @Column(name = "freight", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal freight = BigDecimal.ZERO;

    /**
     * 订单状态:
     * PENDING-待付款, PAID-待发货, SHIPPED-待收货,
     * COMPLETED-已完成, CANCELLED-已取消
     */
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "PENDING";

    /** 收货地址 */
    @Column(name = "shipping_address", length = 500)
    private String shippingAddress;

    /** 收货人姓名 */
    @Column(name = "receiver_name", length = 50)
    private String receiverName;

    /** 收货人电话 */
    @Column(name = "receiver_phone", length = 20)
    private String receiverPhone;

    /** 支付方式: ALIPAY/WECHAT */
    @Column(name = "pay_type", length = 20)
    private String payType;

    /** 支付时间 */
    @Column(name = "pay_time")
    private LocalDateTime payTime;

    /** 发货时间 */
    @Column(name = "ship_time")
    private LocalDateTime shipTime;

    /** 完成时间 */
    @Column(name = "complete_time")
    private LocalDateTime completeTime;

    /** 订单备注 */
    @Column(length = 500)
    private String remark;

    @Column(name = "create_time")
    @Builder.Default
    private LocalDateTime createTime = LocalDateTime.now();

    @Column(name = "update_time")
    @Builder.Default
    private LocalDateTime updateTime = LocalDateTime.now();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<OrderItem> orderItems;
}
