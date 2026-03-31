package vn.thucvu.service.impl;

import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.EAN13Writer;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import vn.thucvu.common.OrderStatus;
import vn.thucvu.controller.request.PaymentMessage;
import vn.thucvu.controller.request.PlaceOrderRequest;
import vn.thucvu.controller.request.UpdateOrderRequest;
import vn.thucvu.controller.response.OrderListResponse;
import vn.thucvu.exception.InvalidDataException;
import vn.thucvu.exception.ResourceNotFoundException;
import vn.thucvu.model.Order;
import vn.thucvu.model.OrderItem;
import vn.thucvu.repository.OrderRepository;
import vn.thucvu.service.OrderService;

import java.awt.image.BufferedImage;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static vn.thucvu.common.OrderStatus.*;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "ORDER-SERVICE")
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${spring.kafka.checkoutOrder}")
    private String checkoutOrderTopic;

    @Override
    public OrderListResponse getAllOrders(OrderStatus status, String sort, int page, int size) {
        log.info("getAllOrders called");

        // Sorting
        Sort.Order order = new Sort.Order(Sort.Direction.ASC, "createdAt");
        if (StringUtils.hasLength(sort)) {
            Pattern pattern = Pattern.compile("(\\w+?)(:)(.*)"); // tencot:asc|desc
            Matcher matcher = pattern.matcher(sort);
            if (matcher.find()) {
                String columnName = matcher.group(1);
                if (matcher.group(3).equalsIgnoreCase("asc")) {
                    order = new Sort.Order(Sort.Direction.ASC, columnName);
                } else {
                    order = new Sort.Order(Sort.Direction.DESC, columnName);
                }
            }
        }

        // Xu ly truong hop FE muon bat dau voi page = 1
        int pageNo = 0;
        if (page > 0) {
            pageNo = page - 1;
        }

        // Paging
        Pageable pageable = PageRequest.of(pageNo, size, Sort.by(order));

        Page<Order> orders;
        if (status != null) {
            orders = orderRepository.findByStatusName(status.name(), pageable);
        } else {
            orders = orderRepository.findAll(pageable);
        }

        return OrderListResponse.builder()
                .pageNumber(page)
                .pageSize(size)
                .totalPages(orders.getTotalPages())
                .totalElements(orders.getTotalElements())
                .orders(orders.toList())
                .build();
    }

    @Override
    public Order getOrderDetail(String orderId) {
        log.info("getOrderDetail called");

        return getOrderById(orderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Order createOrder(PlaceOrderRequest request) {
        log.info("createOrder called");

        Order order = new Order();
        String orderId = String.valueOf(UUID.randomUUID());

        order.setId(orderId);
        order.setCustomerId(request.getCustomerId());
        order.setAmount(request.getAmount());
        order.setCurrency(request.getCurrency());
        order.setPaymentMethod(request.getPaymentMethod());
        order.setStatus(NEW.getValue());
        order.setStatusName(NEW.name());
        order.setCreatedAt(new Date());

        List<OrderItem> orderItems = request.getOrderItems().stream().map(
                item -> OrderItem.builder()
                        .id(UUID.randomUUID().toString())
                        .orderId(orderId)
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .unit(item.getUnit())

                        .build()
        ).toList();
        order.setOrderItems(orderItems);

        Order result = orderRepository.save(order);
        log.info("Order created successfully");

        // push kafka (optional)

        return result;
    }

    @Override
    public void updateOrder(UpdateOrderRequest req) {
        log.info("updateOrder called");

        Order order = getOrderById(req.getId());

        order.setAmount(req.getAmount());
        order.setCurrency(req.getCurrency());
        order.setPaymentMethod(req.getPaymentMethod());
        order.setUpdatedAt(new Date());

        // update status
        if (req.getStatus() != null) {
            order.setStatus(req.getStatus().getValue());
            order.setStatusName(req.getStatus().name());
        }

        // update order items
        List<OrderItem> orderItems = req.getOrderItems().stream().map(
                item -> OrderItem.builder()
                        .orderId(order.getId())
                        .productId(item.getProductId())
                        .quantity(item.getQuantity())
                        .unit(item.getUnit())
                        .price(item.getPrice())
                        .build()
        ).toList();
        order.setOrderItems(orderItems);

        orderRepository.save(order);
        log.info("Order updated");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkoutOrder(String orderId) {
        log.info("checkoutOrder called");

        Order order = getOrderById(orderId);

        // update status before sending Kafka message
        order.setStatus(PAID.getValue());
        order.setStatusName(PAID.name());
        order.setUpdatedAt(new Date());
        orderRepository.save(order);

        // Push message sang payment
        PaymentMessage paymentMessage = new PaymentMessage();
        paymentMessage.setOrderId(orderId);
        paymentMessage.setCustomerId(order.getCustomerId());
        paymentMessage.setAmount(order.getAmount());
        paymentMessage.setCurrency(order.getCurrency());
        paymentMessage.setPaymentMethod(order.getPaymentMethod());

        // convert message to json
        try {
            String jsonMessage = new Gson().toJson(paymentMessage);
            kafkaTemplate.send(checkoutOrderTopic, jsonMessage);
            log.info("checkoutOrder sent message to Payment service message: {}", jsonMessage);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new InvalidDataException("Push message failed");
        }

        log.info("Order checked out");

        // TODO: 1. Sync data with inventory-service
    }

    @Override
    public void changeOrderStatus(String orderId, OrderStatus status) {
        log.info("changeOrderStatus called, status: {}", status.name());

        Order order = getOrderById(orderId);
        order.setStatus(status.getValue());
        order.setStatusName(status.name());
        order.setUpdatedAt(new Date());

        orderRepository.save(order);
        log.info("Order changed status");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(String orderId) {
        log.info("cancelOrder called");

        Order order = getOrderById(orderId);
        order.setStatus(CANCELED.getValue());
        order.setStatusName(CANCELED.name());
        order.setUpdatedAt(new Date());
        orderRepository.save(order);
        log.info("Order cancelled");
    }

    @Override
    public BufferedImage generateQRCodeImage(String text) throws WriterException {
        log.info("Generate QR code image: {}", text);

        QRCodeWriter barcodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = barcodeWriter.encode(text, BarcodeFormat.QR_CODE, 200, 200);

        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }

    /**
     * Generate barcode EAN13 {Mã quốc gia, mã doanh nghiệp, mã sản phẩm và cuối cùng là số kiểm tra}
     * <p>
     * Mã quốc gia: Sử dụng 2 (hoặc 3) ký tự đầu tiên làm mã quốc gia.
     * Mã doanh nghiệp: Sẽ có 5 số nếu chỉ dùng 2 số cho mã quốc gia hoặc có 4 số nếu mã quốc gia dùng đến 3 số.
     * Mã sản phẩm: Với 5 số tiếp theo sẽ là mã sản phẩm của nhà sản xuất.
     * Số kiểm tra: Số cuối cùng là số kiểm tra, phụ thuộc vào 12 số trước nó.
     *
     * @param barcode
     * @return
     * @throws WriterException Format Barcode: https://help.accusoft.com/BarcodeXpress/v13.2/BxNodeJs/ean_13.html
     */
    @Override
    public BufferedImage generateBarCodeImage(String barcode) throws WriterException {
        log.info("Generate Bar code image: {}", barcode);

        // TODO validate EAN13

        EAN13Writer barcodeWriter = new EAN13Writer();
        BitMatrix bitMatrix = barcodeWriter.encode(barcode, BarcodeFormat.EAN_13, 300, 150);

        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }

    /**
     * Get order by id
     *
     * @param orderId
     * @return
     */
    private Order getOrderById(String orderId) {
        return orderRepository.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    }
}
