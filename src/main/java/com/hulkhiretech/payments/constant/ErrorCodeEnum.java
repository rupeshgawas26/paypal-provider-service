package com.hulkhiretech.payments.constant;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.support.BindStatus;

import java.sql.SQLException;

@Getter
public enum ErrorCodeEnum {

    GENERIC_ERROR("30000","Something went wrong. Please try again later."),
    CURRENCY_CODE_REQUIRED("30001","Currency not supported"),
    RETURN_URL_REQUIRED("30002","Return URL is required and cannot be blank"),
    INVALID_REQUEST("30003","Invalid request payload"),
    INVALID_AMOUNT("30004","Amount must be greater than zero"),
    CANCEL_URL_REQUIRED("30005","Cancel URL is required and cannot be blank"),
    PAYPAL_SERVICE_UNAVAILABLE("30006","Paypal service is currently unavailable. Please try again later."),
    PAYPAL_ERROR("30007","<Error as Paypal>"),
    PAYPAL_UNKNOWN_ERROR("30008","Unknown error occurred while processing Paypal request");

    private final String errorCode;
    private final String errorMessage;

    ErrorCodeEnum(String errorCode,String errorMessage){
        this.errorCode=errorCode;
        this.errorMessage=errorMessage;
    }

}
