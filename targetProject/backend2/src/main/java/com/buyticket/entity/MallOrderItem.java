package com.buyticket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("mall_order_item")
public class MallOrderItem {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private Long productId;

    private String productName;

    private String productImage;

    private Integer quantity;

    private BigDecimal price;
}
