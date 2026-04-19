package vn.thucvu.service;

import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import org.springframework.stereotype.Service;
import vn.thucvu.controller.request.ChargeRequest;
import vn.thucvu.controller.request.PaymentIntentRequest;
import vn.thucvu.controller.response.PaymentIntentResponse;

@Service
public interface PaymentService {

    PaymentIntentResponse createPaymentIntent(PaymentIntentRequest request) throws StripeException;

    String createRefund(String paymentIntentId) throws StripeException;

    String confirmPaymentIntent(String paymentId) throws StripeException;

    Charge charge(ChargeRequest request) throws StripeException;

}
