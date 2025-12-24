package com.hulkhiretech.payments.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class PaypalProviderException extends  RuntimeException{
    private static final long serialVersionUID = 145L;
    private final String errorCode;
    private final String errorMessage;
    private final HttpStatus httpStatus;

    public PaypalProviderException(String errorCode, String errorMessage, HttpStatus httpStatus) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.httpStatus = httpStatus;
    }
}
