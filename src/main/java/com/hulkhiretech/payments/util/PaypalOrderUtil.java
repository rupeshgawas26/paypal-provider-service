package com.hulkhiretech.payments.util;

import com.hulkhiretech.payments.paypal.res.error.PaypalErrorDetail;
import com.hulkhiretech.payments.paypal.res.error.PaypalErrorResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PaypalOrderUtil {
    private PaypalOrderUtil() {
    }

    public static String getPaypalErrorSummary(PaypalErrorResponse res) {
        log.info("Generating PayPal error summary from response: {}", res);
        if (res == null) {
            return "Unknown PayPal error.";
        }

        StringBuilder summary = new StringBuilder();

        appendIfPresent(summary, res.getName());
        appendIfPresent(summary, res.getMessage());
        appendIfPresent(summary, res.getError());
        appendIfPresent(summary, res.getErrorDescription());

        if (res.getDetails() != null && !res.getDetails().isEmpty()) {
            PaypalErrorDetail detail = res.getDetails().get(0);
            if (detail != null) {
                appendIfPresent(summary, detail.getField());
                appendIfPresent(summary, detail.getIssue());
                appendIfPresent(summary, detail.getDescription());
            }
        }

        log.info("Generated PayPal error summary: {}", summary.toString());
        return !summary.isEmpty() ? summary.toString() : "Unknown PayPal error.";
    }

    private static void appendIfPresent(StringBuilder sb, String value) {
        if (value != null && !value.isBlank()) {
            if (!sb.isEmpty()) sb.append(" | ");
            sb.append(value.trim());
        }
    }

}
