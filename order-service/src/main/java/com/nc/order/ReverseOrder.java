package com.nc.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ReverseOrder {
    private static final Logger logger = LoggerFactory.getLogger(ReverseOrder.class);
    private static final String ORDER_FAILED_STATUS = "FAILED";
    private final OrderRepository repository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "orders-failure", groupId = "orders-group")
    public void reverseOrder(String event) {
        logger.info("Received event to reverse order: {}", event);

        try {
            OrderEvent orderEvent = deserializeOrderEvent(event);
            processFailedOrder(orderEvent);
        } catch (IOException e) {
            logger.error("Failed to deserialize order event: {}", event, e);
        } catch (Exception e) {
            logger.error("Unexpected error occurred while processing order event: {}", event, e);
        }
    }

    private OrderEvent deserializeOrderEvent(String event) throws IOException {
        return objectMapper.readValue(event, OrderEvent.class);
    }

    private void processFailedOrder(OrderEvent orderEvent) {
        Long orderId = orderEvent.getOrder().getOrderId();
        Optional<Order> orderOptional = repository.findById(orderId);

        if (orderOptional.isPresent()) {
            Order order = orderOptional.get();
            order.setStatus(ORDER_FAILED_STATUS);
            repository.save(order);
            logger.info("Order with ID {} marked as FAILED", orderId);
        } else {
            logger.warn("Order with ID {} not found for reversal", orderId);
        }
    }
}