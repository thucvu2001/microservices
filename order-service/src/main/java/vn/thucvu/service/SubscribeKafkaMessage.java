package vn.thucvu.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import vn.thucvu.common.OrderStatus;
import vn.thucvu.model.Order;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "SUBSCRIBE-KAFKA-MESSAGE")
public class SubscribeKafkaMessage {

    private final OrderService orderService;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${spring.kafka.update-inventory}")
    private String updateInventoryTopic;

    @Value("${spring.kafka.order-success}")
    private String orderSuccessTopic;

    /**
     * Listen event from payment success
     */
    @KafkaListener(topics = "${spring.kafka.update-order-status}", groupId = "update-order-status-group")
    public void handleEventChangeOrderStatus(String message) throws JsonProcessingException {
        log.info("handleEventChangeOrderStatus called, message: {}", message);

        OrderMessage orderMessage = new ObjectMapper().readValue(message, OrderMessage.class);

        // change order status=PAID
        orderService.changeOrderStatus(orderMessage.getOrderId(), orderMessage.getStatus());

        // synchronize data with inventory-service
        Order order = orderService.getOrderDetail(orderMessage.getOrderId());
        kafkaTemplate.send(updateInventoryTopic, new ObjectMapper().writeValueAsString(order));
        log.info("Send order to inventory-service successfully");

        // Push notification to customer
        kafkaTemplate.send(orderSuccessTopic, order.getCustomerId().toString());
        log.info("Send order id to notification-service successfully");

        // TODO: Send invoice to email of customer
    }

    @Getter
    @Setter
    private static class OrderMessage {
        private String orderId;
        private OrderStatus status;
    }
}
