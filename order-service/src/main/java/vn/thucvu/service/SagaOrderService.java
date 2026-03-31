package vn.thucvu.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import vn.thucvu.common.Currency;
import vn.thucvu.common.OrderStatus;
import vn.thucvu.common.PaymentMethod;
import vn.thucvu.controller.request.PaymentIntentRequest;
import vn.thucvu.controller.request.PlaceOrderRequest;
import vn.thucvu.controller.request.SaleOrderCreationRequest;
import vn.thucvu.controller.request.SaleOrderItemCreationRequest;
import vn.thucvu.controller.response.ApiResponse;
import vn.thucvu.controller.response.PaymentIntentResponse;
import vn.thucvu.model.Order;
import vn.thucvu.model.OrderItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "SAGA-ORDER-SERVICE")
public class SagaOrderService {

    private final OrderService orderService;
    private final RestTemplate restTemplate;

    @Value("${api.internal.paymentUrl}")
    private String paymentUrl;
    @Value("${api.internal.saleOrderUrl}")
    private String saleOrderUrl;

    public String createSagaOrder(PlaceOrderRequest request) {
        log.info("createSagaOrder called");

        String orderId = "";
        String saleOrderId = "";
        String paymentId = "";
        try {
            // Create order
            Order order = orderService.createOrder(request);
            orderId = order.getId();

            // Call process init payment intent
            PaymentIntentResponse response = payOrder(order);
            paymentId = response.getPaymentId();

            // Synchronize data with inventory service
            saleOrderId = createSaleOrder(order);

            // return clientSecret for frontend continue process confirm payment with Stripe
            return response.getClientSecret();
        } catch (Exception e) {
            log.error("createSagaOrder error", e);

            if (StringUtils.hasLength(orderId)) {
                orderService.cancelOrder(orderId);
            }
            if (StringUtils.hasLength(paymentId)) {
                refund(paymentId);
            }
            if (StringUtils.hasLength(saleOrderId)) {
                cancelSaleOrder(saleOrderId);
            }
            throw e;
        }
    }

    /**
     * Process payment
     *
     * @param order
     * @return
     */
    private PaymentIntentResponse payOrder(Order order) {
        PaymentIntentRequest request = new PaymentIntentRequest();
        request.setCustomerId(order.getCustomerId());
        request.setOrderId(order.getId());
        request.setPaymentMethod(PaymentMethod.CARD);
        request.setAmount(50000L);
        request.setCurrency(Currency.USD);
        request.setDescription("saga_" + order.getCustomerId() + "_" + order.getId());

        try {
            return restTemplate.postForObject(paymentUrl + "/create-payment-intent", request, PaymentIntentResponse.class);
        } catch (RestClientException e) {
            throw e;
        }
    }

    /**
     * Refund for order was canceled
     *
     * @param paymentId
     */
    private void refund(String paymentId) {
        log.info("refund called");

        Map response = restTemplate.postForObject(paymentUrl + "/refund?paymentId=" + paymentId,null, Map.class);
        log.info("Refund response: {}", response);
    }

    /**
     * Update inventory
     *
     * @param order
     */
    private String createSaleOrder(Order order) {
        log.info("createSaleOrder called");

        SaleOrderCreationRequest orderRequest = new SaleOrderCreationRequest();
        orderRequest.setId(order.getId());
        orderRequest.setCustomerId(order.getCustomerId());
        orderRequest.setStatus(OrderStatus.PENDING);
        orderRequest.setTotalAmount(order.getAmount());
        orderRequest.setCurrency(order.getCurrency());
        orderRequest.setPaymentMethod(order.getPaymentMethod());

        List<SaleOrderItemCreationRequest> items = new ArrayList<>();
        for (OrderItem item : order.getOrderItems()) {
            SaleOrderItemCreationRequest orderItemRequest = new SaleOrderItemCreationRequest();
            orderItemRequest.setSalesId(order.getId());
            orderItemRequest.setProductId(item.getProductId());
            orderItemRequest.setQuantity(item.getQuantity());
            orderItemRequest.setPrice(item.getPrice());
            items.add(orderItemRequest);
        }
        orderRequest.setItems(items);

        try {
            ApiResponse response = restTemplate.postForObject(saleOrderUrl + "/add", orderRequest, ApiResponse.class);
            log.info("Created saleOrder: {}", response);

            assert response != null;
            return response.getData() == null ? "" : response.getData().toString();
        } catch (RestClientException e) {
            throw e;
        }

    }

    /**
     * Cancel sale order from inventory
     *
     * @param orderId
     */
    private void cancelSaleOrder(String orderId) {
        log.info("cancelSaleOrder called");

        ApiResponse response = restTemplate.patchForObject(saleOrderUrl + "/cancel/" + orderId,
                null,
                ApiResponse.class);
        log.info("cancelSaleOrder response: {}", response);
    }
}
