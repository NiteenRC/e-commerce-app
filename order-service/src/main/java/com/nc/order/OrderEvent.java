package com.nc.order;

import lombok.Data;

@Data
public class OrderEvent {
    private String type;
    private CustomerOrder order;
}
