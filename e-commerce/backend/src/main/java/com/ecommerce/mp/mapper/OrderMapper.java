package com.ecommerce.mp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ecommerce.mp.entity.MpOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单 Mapper 接口 - 继承 MyBatis Plus BaseMapper，自动获得 CRUD 能力
 */
@Mapper
public interface OrderMapper extends BaseMapper<MpOrder> {
}
