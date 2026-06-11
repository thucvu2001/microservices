package vn.thucvu.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import vn.thucvu.common.Currency;
import vn.thucvu.common.OrderStatus;
import vn.thucvu.common.PaymentMethod;
import vn.thucvu.model.SaleOrder;
import vn.thucvu.model.SaleOrderItem;
import vn.thucvu.repository.SaleOrderItemRepository;
import vn.thucvu.repository.SaleOrderRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "SUBSCRIBE-KAFKA-MESSAGE")
public class SubscribeKafkaMessage {

    private final SaleOrderRepository saleOrderRepository;

    private final SaleOrderItemRepository saleOrderItemRepository;

    /**
     * Listen event from payment success
     */
    @KafkaListener(topics = "${spring.kafka.update-inventory}", groupId = "update-inventory-group")
    public void handleEventUpdateInventory(String message) throws IOException {
        log.info("handleEventUpdateInventory called, message: {}", message);

        // Convert JSON to Object
        OrderMessage orderMessage = new ObjectMapper().readValue(message, OrderMessage.class);

        SaleOrder saleOrder = new SaleOrder();
        saleOrder.setId(orderMessage.getId());
        saleOrder.setCustomerId(orderMessage.getCustomerId());
        saleOrder.setTotalAmount(orderMessage.getAmount());
        saleOrder.setCurrency(orderMessage.getCurrency());
        saleOrder.setPaymentMethod(orderMessage.getPaymentMethod());
        saleOrder.setStatus(OrderStatus.valueOf(orderMessage.getStatusName()));

        SaleOrder result = saleOrderRepository.save(saleOrder);

        log.info("SaleOrder saved");

        if (result.getId() != null) {
            List<SaleOrderItem> saleOrderItems = new ArrayList<>();
            for (OrderItem item : orderMessage.getOrderItems()) {
                SaleOrderItem saleOrderItem = new SaleOrderItem();
                saleOrderItem.setSalesId(result.getId());
                saleOrderItem.setProductId(item.getProductId());
                saleOrderItem.setQuantity(item.getQuantity());
                saleOrderItem.setPrice(item.getPrice());
                saleOrderItems.add(saleOrderItem);
            }

            saleOrderItemRepository.saveAll(saleOrderItems);
            log.info("saleOrderItems saved");
        }

        log.info("saleOrder created id: {}", result.getId());

    }


    @Getter
    @Setter
    private static class OrderMessage {
        private String id;
        private Long customerId;
        private Long amount;
        private Currency currency;
        private PaymentMethod paymentMethod;
        private int status;
        private String statusName;
        private Date createdAt;
        private Date updatedAt;
        // Nested object
        private List<OrderItem> orderItems;
    }

    @Getter
    @Setter
    private static class OrderItem {
        private String id;
        private String orderId;
        private Long productId;
        private String productName;
        private Integer quantity;
        private Long price;
        private String unit;
    }
}
