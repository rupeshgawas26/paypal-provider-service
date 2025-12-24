package com.hulkhiretech.payments.paypal.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class PaypalOrder {

    private String id;

    private String status;

    @JsonProperty("payment_source")
    private PaymentSource paymentSource;

    @JsonProperty("links")
    private List<PaymentLink> links;
}
