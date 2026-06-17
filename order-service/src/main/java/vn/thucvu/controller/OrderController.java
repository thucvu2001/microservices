package vn.thucvu.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.thucvu.common.OrderStatus;
import vn.thucvu.controller.request.PlaceOrderRequest;
import vn.thucvu.controller.request.UpdateOrderRequest;
import vn.thucvu.controller.response.ApiResponse;
import vn.thucvu.model.Order;
import vn.thucvu.service.OrderService;

import java.awt.image.BufferedImage;

import static org.springframework.http.HttpStatus.*;

@RestController
@Slf4j(topic = "ORDER-CONTROLLER")
public record OrderController(OrderService orderService) {

    @GetMapping("/list")
    public ApiResponse getAllOrders(@RequestParam(required = false) OrderStatus status,
                                    @RequestParam(required = false) String sort,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "20") int size) {
        log.info("Get order list");

        return ApiResponse.builder()
                .status(OK.value())
                .message("Order list")
                .data(orderService.getAllOrders(status, sort, page, size))
                .build();
    }

    @GetMapping("/{orderId}")
    public ApiResponse getOrderDetail(@PathVariable String orderId) {
        log.info("Get order detail, id={}", orderId);

        return ApiResponse.builder()
                .status(OK.value())
                .message("Order detail")
                .data(orderService.getOrderDetail(orderId))
                .build();
    }

    @PostMapping("/add")
    public ApiResponse placeOrder(@RequestBody PlaceOrderRequest request) {
        log.info("Place order, orderRequest: {}", request);

        Order order = orderService.createOrder(request);

        return ApiResponse.builder()
                .status(CREATED.value())
                .message("Order created successfully")
                .data(order.getId())
                .build();
    }

    @PutMapping("/upd")
    public ApiResponse updateOrder(@RequestBody UpdateOrderRequest request) {
        log.info("Update order, id={}", request.getId());

        orderService.updateOrder(request);

        return ApiResponse.builder()
                .status(ACCEPTED.value())
                .message("Order updated successfully")
                .build();
    }

    @PatchMapping("/checkout/{orderId}")
    public ApiResponse checkoutOrder(@PathVariable String orderId) {
        log.info("Checkout order, id={}", orderId);

        orderService.checkoutOrder(orderId);

        return ApiResponse.builder()
                .status(ACCEPTED.value())
                .message("Order checkout successfully")
                .build();
    }

    @PatchMapping("/cancel/{orderId}")
    public ApiResponse cancelOrder(@PathVariable String orderId) {
        log.info("Cancel order, id={}", orderId);

        orderService.cancelOrder(orderId);

        return ApiResponse.builder()
                .status(ACCEPTED.value())
                .message("Order cancelled successfully")
                .build();
    }

    @PostMapping(path = "/qrcode", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<BufferedImage> generateQRCode(@RequestParam String text) throws Exception {
        log.info("Generate qrcode request");
        return ResponseEntity.ok(orderService.generateQRCodeImage(text));
    }

    @PostMapping(path = "/barcode", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<BufferedImage> generateBarcode(@RequestParam String barcode) throws Exception {
        log.info("Generate barcode request");
        return ResponseEntity.ok(orderService.generateBarCodeImage(barcode));
    }

}
