package vn.thucvu.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.thucvu.controller.response.PaymentIntentResponse;

@Service
@Slf4j
public class PaymentService {

    @Autowired
    public PaymentService(@Value("${stripe.secret-key}") String secretKey) {
        Stripe.apiKey = secretKey;
    }

    public PaymentIntentResponse createPaymentIntent(Long amount, String currency) throws StripeException {
        log.info("Stripe service createPaymentIntent");

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amount * 100L)
                .setCurrency(currency)
                .setAutomaticPaymentMethods(PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                        .setEnabled(true)
                        .build()
                )
                .build();

        PaymentIntent paymentIntent = PaymentIntent.create(params);

        return PaymentIntentResponse.builder()
                .paymentId(paymentIntent.getId())
                .clientSecret(paymentIntent.getClientSecret())
                .build();

    }

    public void createRefund(String paymentIntentId) throws StripeException {
        log.info("Stripe service createRefund");

        RefundCreateParams params = RefundCreateParams.builder()
                .setPaymentIntent(paymentIntentId)
                .build();

        Refund refund = Refund.create(params);

    }

    public String confirmPaymentIntent(String paymentIntentId) throws StripeException {
        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
        PaymentIntent confirmedIntent = paymentIntent.confirm();
        return confirmedIntent.getStatus();
    }
}
