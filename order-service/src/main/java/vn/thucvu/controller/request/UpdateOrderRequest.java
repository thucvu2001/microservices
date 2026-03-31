package vn.thucvu.controller.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.ToString;
import vn.thucvu.common.Currency;
import vn.thucvu.common.OrderStatus;
import vn.thucvu.common.PaymentMethod;

import java.io.Serializable;
import java.util.List;

@Getter
@ToString
public class UpdateOrderRequest implements Serializable {

    private String id;

    @NotNull(message = "amount must be not null")
    private Long amount;

    @NotNull(message = "currency must be not null")
    private Currency currency;

    @NotNull(message = "paymentMethod must be not null")
    private PaymentMethod paymentMethod;

    private OrderStatus status;

    @NotEmpty(message = "orderItems must be not empty")
    private List<OrderItemRequest> orderItems;

}
