package vn.thucvu.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import vn.thucvu.common.Currency;
import vn.thucvu.common.PaymentMethod;

import java.io.Serializable;

@Getter
public class ChargeRequest implements Serializable {

    @NotBlank(message = "token must be not blank")
    private String token;

    // @NotNull(message = "customerId must be not null")
    private Long customerId;

    @NotNull(message = "paymentMethod must be not blank")
    private PaymentMethod paymentMethod;

    @NotNull(message = "amount must be not null")
    private Long amount;

    @NotNull(message = "currency must be not null")
    private Currency currency;

    @NotBlank(message = "description must be not blank")
    private String description;
}
