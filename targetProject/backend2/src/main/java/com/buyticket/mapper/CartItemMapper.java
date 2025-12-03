package com.buyticket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.buyticket.entity.CartItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CartItemMapper extends BaseMapper<CartItem> {
}
