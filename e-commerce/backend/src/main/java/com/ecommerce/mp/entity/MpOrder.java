package com.ecommerce.mp.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单实体类 (MyBatis Plus) - 对应 orders 表
 */
@Data
@TableName("orders")
public class MpOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 订单编号 */
    private String orderNo;

    /** 用户ID */
    private Long userId;

    /** 订单总金额 */
    private BigDecimal totalAmount;

    /** 实付金额 */
    private BigDecimal payAmount;

    /** 运费 */
    private BigDecimal freight;

    /** 状态: PENDING/PAID/SHIPPED/COMPLETED/CANCELLED */
    private String status;

    /** 收货地址 */
    private String shippingAddress;

    /** 收货人 */
    private String receiverName;

    /** 收货电话 */
    private String receiverPhone;

    /** 支付方式 */
    private String payType;

    /** 支付时间 */
    private LocalDateTime payTime;

    /** 发货时间 */
    private LocalDateTime shipTime;

    /** 完成时间 */
    private LocalDateTime completeTime;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 订单项列表（非数据库字段） */
    @TableField(exist = false)
    private List<MpOrderItem> orderItems;
}
