package com.nc.order;

import com.nc.order.exception.OrderProcessingException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    @Transactional
    public Order processOrder(CustomerOrder customerOrder) {
        Order order = createOrder(customerOrder);
        publishOrderEvent(order);
        return order;
    }

    private Order createOrder(CustomerOrder customerOrder) {
        Order order = convertToOrder(customerOrder);
        order.setStatus("CREATED");
        try {
            return orderRepository.save(order);
        } catch (Exception e) {
            handleOrderCreationFailure(order, e);
            throw new OrderProcessingException("Failed to process order", e);
        }
    }

    private void publishOrderEvent(Order order) {
        try {
            CustomerOrder convertedCustomerOrder = convertToCustomerOrder(order);
            OrderEvent event = new OrderEvent();
            event.setOrder(convertedCustomerOrder);
            event.setType("ORDER_CREATED");
            kafkaTemplate.send("new-orders", event);
            logger.info("Order created and event published: {}", order);
        } catch (Exception e) {
            logger.error("Failed to publish order event for order: {}", order.getId(), e);
        }
    }

    private Order convertToOrder(CustomerOrder customerOrder) {
        Order order = new Order();
        order.setAmount(customerOrder.getAmount());
        order.setItem(customerOrder.getItem());
        order.setQuantity(customerOrder.getQuantity());
        return order;
    }

    private CustomerOrder convertToCustomerOrder(Order order) {
        CustomerOrder customerOrder = new CustomerOrder();
        customerOrder.setAmount(order.getAmount());
        customerOrder.setItem(order.getItem());
        customerOrder.setQuantity(order.getQuantity());
        customerOrder.setOrderId(order.getId());
        return customerOrder;
    }

    private void handleOrderCreationFailure(Order order, Exception exception) {
        logger.error("Error processing order: {}", order, exception);
        order.setStatus("FAILED");
        orderRepository.save(order);
    }
}