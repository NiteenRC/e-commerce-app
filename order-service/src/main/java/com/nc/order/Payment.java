package com.nc.order;

import lombok.Data;

@Data
public class Payment {
    private String mode;
    private Long orderId;
    private double amount;
}