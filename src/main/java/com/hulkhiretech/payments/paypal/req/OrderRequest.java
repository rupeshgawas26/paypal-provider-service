package com.hulkhiretech.payments.paypal.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class OrderRequest {
    @JsonProperty("intent")
    private String intent;

    @JsonProperty("purchase_units")
    private List<PurchaseUnit> purchaseUnits;

    @JsonProperty("payment_source")
    private PaymentSource paymentSource;
}
