package vn.thucvu.controller.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import vn.thucvu.common.Currency;
import vn.thucvu.common.OrderStatus;
import vn.thucvu.common.PaymentMethod;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@ToString
public class SaleOrderCreationRequest implements Serializable {

    @NotNull(message = "id must be not null")
    private String id;

    @NotNull(message = "customerId must be not null")
    private Long customerId;

    @NotNull(message = "status must be not null")
    private OrderStatus status;

    @NotNull(message = "totalAmount must be not null")
    private Long totalAmount;

    @NotNull(message = "currency must be not null")
    private Currency currency;

    @NotNull(message = "paymentMethod must be not null")
    private PaymentMethod paymentMethod;

    @NotNull(message = "items must be not null")
    private List<SaleOrderItemCreationRequest> items;
}