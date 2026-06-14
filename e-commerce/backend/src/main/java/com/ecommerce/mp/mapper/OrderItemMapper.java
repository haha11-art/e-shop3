package com.ecommerce.mp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ecommerce.mp.entity.MpOrderItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单项 Mapper 接口
 */
@Mapper
public interface OrderItemMapper extends BaseMapper<MpOrderItem> {
}
