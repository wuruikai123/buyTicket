package com.buyticket.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class MallOrderCreateRequest {
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private List<MallItemRequest> items;

    @Data
    public static class MallItemRequest {
        private Long productId;
        private Integer quantity;
        private BigDecimal price;
    }
}
