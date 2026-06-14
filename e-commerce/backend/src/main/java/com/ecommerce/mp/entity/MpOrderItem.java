package com.ecommerce.mp.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单项实体类 (MyBatis Plus) - 对应 order_items 表
 */
@Data
@TableName("order_items")
public class MpOrderItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 订单ID */
    private Long orderId;

    /** 商品ID */
    private Long productId;

    /** 商品名称快照 */
    private String productName;

    /** 商品图片快照 */
    private String productImage;

    /** 购买单价 */
    private BigDecimal unitPrice;

    /** 购买数量 */
    private Integer quantity;

    /** 小计金额 */
    private BigDecimal totalPrice;
}
