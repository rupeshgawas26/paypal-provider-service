package com.hulkhiretech.payments.exception;

import com.hulkhiretech.payments.constant.ErrorCodeEnum;
import com.hulkhiretech.payments.pojo.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(PaypalProviderException.class)
    public ResponseEntity<ErrorResponse> handlePaypalProviderException(PaypalProviderException ex) {
        log.error("Handling PaypalProviderException ", ex.getErrorMessage(),ex);
        ErrorResponse errorResponse = new ErrorResponse(
                ex.getErrorCode(), ex.getErrorMessage());
        return new ResponseEntity<>(errorResponse, ex.getHttpStatus());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        log.error("Handling Generic Exception ", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(ErrorCodeEnum.GENERIC_ERROR.getErrorCode(),
                ErrorCodeEnum.GENERIC_ERROR.getErrorMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
