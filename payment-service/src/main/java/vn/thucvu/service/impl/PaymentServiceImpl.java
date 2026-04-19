package vn.thucvu.service.impl;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.param.ChargeCreateParams;
import com.stripe.param.RefundCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.thucvu.common.Currency;
import vn.thucvu.common.PaymentMethod;
import vn.thucvu.common.TransactionStatus;
import vn.thucvu.controller.request.ChargeRequest;
import vn.thucvu.controller.request.PaymentIntentRequest;
import vn.thucvu.controller.response.PaymentIntentResponse;
import vn.thucvu.model.Transaction;
import vn.thucvu.service.PaymentService;
import vn.thucvu.service.TransactionService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j(topic = "PAYMENT-SERVICE")
public class PaymentServiceImpl implements PaymentService {

    private final TransactionService transactionService;

    @Autowired
    public PaymentServiceImpl(@Value("${stripe.secret-key}") String secretKey, TransactionService transactionService) {
        Stripe.apiKey = secretKey;
        this.transactionService = transactionService;
    }

    /**
     * PaymentIntent is a key concept used to manage the lifecycle of a payment. It represents an attempt to collect payment from a customer
     * and is designed to handle complex payment flows, including multi-step authentication (e.g., 3D Secure) and retries for failed payments.
     *
     * @param request
     * @return return the clientSecret to the frontend
     * @throws StripeException
     */
    @Override
    public PaymentIntentResponse createPaymentIntent(PaymentIntentRequest request) throws StripeException {
        log.info("createPaymentIntent");

        List<String> paymentMethodTypes = new ArrayList<>();
        paymentMethodTypes.add(request.getPaymentMethod().getValue());

        Map<String, Object> params = new HashMap<>();
        params.put("amount", request.getAmount());
        params.put("currency", request.getCurrency());
        params.put("payment_method_types", paymentMethodTypes);

        PaymentIntent paymentIntent = PaymentIntent.create(params);
        log.info("PaymentIntent created, id: {}", paymentIntent.getId());

        // Save transaction
        saveTransaction(request.getCustomerId(), paymentIntent.getId(), request.getPaymentMethod(), request.getAmount(), request.getCurrency(), request.getDescription());

        return PaymentIntentResponse.builder()
                .paymentId(paymentIntent.getId())
                .clientSecret(paymentIntent.getClientSecret()) // Return the clientSecret to the frontend
                .build();
    }

    /**
     * Refund money to customer by paymentIntentId
     */
    @Override
    public String createRefund(String paymentIntentId) throws StripeException {
        log.info("createRefund for paymentIntentId: {}", paymentIntentId);

        RefundCreateParams params = RefundCreateParams.builder()
                .setPaymentIntent(paymentIntentId)
                .build();

        Refund refund = Refund.create(params);

        return refund.getId();
    }

    /**
     * Confirm Payment Intent
     */
    @Override
    public String confirmPaymentIntent(String paymentId) throws StripeException {
        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentId);
        PaymentIntent confirmedIntent = paymentIntent.confirm();
        return confirmedIntent.getStatus();
    }

    /**
     * Charge refers to the process of charging a customer's credit or debit card through the Stripe payment platform.
     * It is a fundamental action in Stripe's payment processing system, allowing businesses to collect payments from their customers.
     * When a transaction occurs, Stripe creates a "charge" to initiate and manage the payment process.
     */
    /**
     * Charge refers to the process of charging a customer's credit or debit card through the Stripe payment platform.
     * It is a fundamental action in Stripe's payment processing system, allowing businesses to collect payments from their customers.
     * When a transaction occurs, Stripe creates a "charge" to initiate and manage the payment process.
     *
     * @param request
     * @return
     * @throws StripeException
     */
    @Override
    public Charge charge(ChargeRequest request) throws StripeException {
        // Create a charge request
        ChargeCreateParams params = ChargeCreateParams.builder()
                .setAmount(request.getAmount())
                .setCurrency(String.valueOf(request.getCurrency()).toLowerCase())
                .setDescription(request.getDescription())
                .setSource(request.getToken()) // Token from the frontend
                .build();

        Charge charge = Charge.create(params);

        // Save transaction
        saveTransaction(request.getCustomerId(), charge.getId(), request.getPaymentMethod(), request.getAmount(), request.getCurrency(), request.getDescription());

        return charge;
    }

    /**
     * Save transaction
     *
     * @param customerId
     * @param paymentId
     * @param paymentMethod
     * @param amount
     * @param currency
     * @param description
     */
    private void saveTransaction(Long customerId, String paymentId, PaymentMethod paymentMethod, Long amount, Currency currency, String description) {
        log.info("saveTransaction for paymentId: {}", paymentId);

        Transaction transaction = new Transaction();
        if (customerId != null) {
            transaction.setCustomerId(customerId);
        }
        transaction.setPaymentId(paymentId);
        transaction.setPaymentMethod(paymentMethod);
        transaction.setAmount(amount);
        transaction.setCurrency(currency);
        transaction.setDescription(description);
        transaction.setStatus(TransactionStatus.CREATED);

        transactionService.createTransaction(transaction);
    }
}
