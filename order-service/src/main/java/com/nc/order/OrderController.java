package com.nc.order;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
    private final OrderService orderService;

    @PostMapping
    public OrderResponseDTO createOrder(@RequestBody CustomerOrder customerOrder) {
        logger.info("Received order creation request: {}", customerOrder);
        Order order = orderService.createAndProcessOrder(customerOrder);
        return new OrderResponseDTO(order.getId(), order.getStatus());
    }
}