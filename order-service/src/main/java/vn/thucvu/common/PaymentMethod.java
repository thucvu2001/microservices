package vn.thucvu.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PaymentMethod {
    @JsonProperty("money")
    MONEY("money"),
    @JsonProperty("card")
    CARD("card"),
    @JsonProperty("bank_transfer")
    BANK_TRANSFER("bank_transfer"),
    @JsonProperty("digital_wallet")
    DIGITAL_WALLET("digital_wallet"),
    @JsonProperty("stripe")
    STRIPE("stripe");

    private final String value;

}
