package vn.thucvu.controller;

import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.thucvu.controller.response.ApiResponse;
import vn.thucvu.controller.response.PaymentIntentResponse;
import vn.thucvu.service.PaymentService;

@RequiredArgsConstructor
@RestController
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create-payment-intent")
    public ApiResponse createPaymentIntent(@RequestParam Long amount, @RequestParam String currency) throws StripeException {
        log.info("Create payment intent, amount={}, currency={}", amount, currency);

        PaymentIntentResponse response = paymentService.createPaymentIntent(amount, currency);

        return ApiResponse.builder()
                .status(200)
                .message("Payment intent created successfully")
                .data(response)
                .build();
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
