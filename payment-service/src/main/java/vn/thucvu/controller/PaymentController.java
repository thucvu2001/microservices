package vn.thucvu.controller;

import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.thucvu.controller.request.ChargeRequest;
import vn.thucvu.controller.request.PaymentIntentRequest;
import vn.thucvu.controller.response.ApiResponse;
import vn.thucvu.controller.response.PaymentIntentResponse;
import vn.thucvu.service.impl.PaymentServiceImpl;

import java.util.Map;

@RequiredArgsConstructor
@RestController
@Slf4j
public class PaymentController {

    private final PaymentServiceImpl paymentService;

    @Operation(summary = "Create payment intent", description = "API Pay for order")
    @PostMapping("/create-payment-intent")
    public ResponseEntity<Map<String, String>> createPaymentIntent(@RequestBody PaymentIntentRequest request) throws StripeException {
        log.info("Create payment intent");

        PaymentIntentResponse response = paymentService.createPaymentIntent(request);

        return ResponseEntity.ok(Map.of("clientSecret", response.getClientSecret()));
    }

    @Operation(summary = "Charge with credit or debit card", description = "API for charge with order online")
    @PostMapping("/charge")
    public ResponseEntity<Map<String, String>> charge(@RequestBody ChargeRequest request) throws StripeException {
        log.info("Charge request received");

        Charge charge = paymentService.charge(request);

        return ResponseEntity.ok(Map.of("status", charge.getStatus()));
    }

    @PostMapping("/refund")
    public ApiResponse createRefund(@RequestParam String paymentIntentId) throws StripeException {
        log.info("Create refund, paymentIntentId={}", paymentIntentId);

        paymentService.createRefund(paymentIntentId);

        return ApiResponse.builder()
                .status(200)
                .message("Refund created successfully")
                .build();
    }

    @PostMapping("/confirm-payment")
    public ApiResponse confirmPaymentIntent(@RequestParam String paymentIntentId) throws StripeException {
        log.info("Confirm payment intent, paymentIntentId={}", paymentIntentId);

        String response = paymentService.confirmPaymentIntent(paymentIntentId);

        return ApiResponse.builder()
                .status(200)
                .message("Payment intent confirmed successfully")
                .data(response)
                .build();
    }
}
