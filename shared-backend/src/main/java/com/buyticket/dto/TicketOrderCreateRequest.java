package com.buyticket.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class TicketOrderCreateRequest {
    private Long exhibitionId;
    private String contactName;
    private String contactPhone;
    private BigDecimal totalAmount;
    private List<TicketItemRequest> items;
    private List<TicketBuyerRequest> buyers;

    @Data
    public static class TicketItemRequest {
        private LocalDate date;
        private String timeSlot;
        private Integer quantity;
        private BigDecimal unitPrice;
    }

    @Data
    public static class TicketBuyerRequest {
        private String name;
        private String idCard;
    }
}
