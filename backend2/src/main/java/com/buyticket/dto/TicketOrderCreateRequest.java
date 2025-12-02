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

    @Data
    public static class TicketItemRequest {
        private LocalDate date;
        private String timeSlot;
        private Integer quantity;
        private BigDecimal unitPrice;
    }
}
