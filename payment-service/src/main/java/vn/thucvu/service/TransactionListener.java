package vn.thucvu.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import vn.thucvu.common.Currency;
import vn.thucvu.common.PaymentMethod;
import vn.thucvu.common.TransactionStatus;
import vn.thucvu.model.Transaction;
import vn.thucvu.repository.TransactionRepository;

import java.io.IOException;
import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "TRANSACTION-LISTENER")
public class TransactionListener {

    private final TransactionRepository transactionRepository;

    @KafkaListener(topics = "${spring.kafka.checkoutOrder}", groupId = "checkout-order-group")
    public void checkoutOrderTopic(String message) throws IOException {
        log.info("Checkout order topic: {}", message);

        PaymentMessage paymentMessage = new ObjectMapper().readValue(message, PaymentMessage.class);

        Transaction transaction = new Transaction();
        if (paymentMessage.getCustomerId() != null) {
            transaction.setCustomerId(paymentMessage.getCustomerId());
        }
        transaction.setPaymentId("ck_" + Instant.now().getEpochSecond());
        transaction.setPaymentMethod(paymentMessage.getPaymentMethod());
        transaction.setAmount(paymentMessage.getAmount());
        transaction.setCurrency(paymentMessage.getCurrency());
        transaction.setDescription("Checkout orderId_" + paymentMessage.getOrderId());
        transaction.setStatus(TransactionStatus.SUCCEEDED);

        transactionRepository.save(transaction);
    }

    @Getter
    private static class PaymentMessage {

        private String orderId;
        private Long customerId;
        private Long amount;
        private Currency currency;
        private PaymentMethod paymentMethod;
    }
}