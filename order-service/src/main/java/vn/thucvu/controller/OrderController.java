package vn.thucvu.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.thucvu.common.OrderStatus;
import vn.thucvu.controller.response.ApiResponse;
import vn.thucvu.service.OrderService;

import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
@Slf4j(topic = "ORDER-CONTROLLER")
public class OrderController {

    private final OrderService orderService;

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
}
