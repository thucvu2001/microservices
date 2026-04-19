package vn.thucvu.controller.response;

import lombok.*;
import vn.thucvu.common.Currency;
import vn.thucvu.common.PaymentMethod;
import vn.thucvu.common.TransactionStatus;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse implements Serializable {

    private Long id;
    private Long customerId;
    private String paymentId;
    private PaymentMethod paymentMethod;
    private Long amount;
    private Currency currency;
    private String description;
    private TransactionStatus status;
    private Date createdAt;
    private Date updatedAt;
}
