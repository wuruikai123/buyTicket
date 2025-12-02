package com.buyticket.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CartItemVO {
    private Long id;          // 购物车项ID
    private Long productId;
    private String productName;
    private BigDecimal price;
    private Integer quantity;
    private String coverImage;
}
