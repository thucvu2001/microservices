package vn.thucvu.controller.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.thucvu.common.Currency;
import vn.thucvu.common.PaymentMethod;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMessage {

    private String orderId;
    private Long customerId;
    private Long amount;
    private Currency currency;
    private PaymentMethod paymentMethod;
}
