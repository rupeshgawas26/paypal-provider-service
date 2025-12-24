package com.hulkhiretech.payments.paypal.res.error;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PaypalErrorLink {

    private String href;
    private String rel;

    @JsonProperty("encType")
    private String encType;
}

