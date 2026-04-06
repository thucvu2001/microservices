package vn.thucvu.controller.response;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PaymentIntentResponse {
    private String paymentId;
    private String clientSecret;
}
