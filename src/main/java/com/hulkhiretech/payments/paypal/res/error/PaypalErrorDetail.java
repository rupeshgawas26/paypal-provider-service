package com.hulkhiretech.payments.paypal.res.error;

import lombok.Data;

@Data
public class PaypalErrorDetail {
    private String field;
    private String location;
    private String issue;
    private String description;
}
