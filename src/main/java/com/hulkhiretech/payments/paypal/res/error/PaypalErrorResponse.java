package com.hulkhiretech.payments.paypal.res.error;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class PaypalErrorResponse {

    private String error;

    @JsonProperty("error_description")
    private String errorDescription;

    private String name;
    private String message;

    @JsonProperty("debug_id")
    private String debugId;

    private List<PaypalErrorDetail> details;
    private List<PaypalErrorLink> links;
}

