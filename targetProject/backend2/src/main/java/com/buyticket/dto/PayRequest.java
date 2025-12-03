package com.buyticket.dto;

import lombok.Data;

@Data
public class PayRequest {
    private Long orderId;
    private String type; // "ticket" or "mall"
    private String password;
}
