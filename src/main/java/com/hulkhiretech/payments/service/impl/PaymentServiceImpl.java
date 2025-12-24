package com.hulkhiretech.payments.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hulkhiretech.payments.constant.Constant;
import com.hulkhiretech.payments.constant.ErrorCodeEnum;
import com.hulkhiretech.payments.exception.PaypalProviderException;
import com.hulkhiretech.payments.helper.CaptureOrderHelper;
import com.hulkhiretech.payments.helper.CreateOrderHelper;
import com.hulkhiretech.payments.http.HttpRequest;
import com.hulkhiretech.payments.http.HttpServiceEngine;
import com.hulkhiretech.payments.paypal.req.*;
import com.hulkhiretech.payments.paypal.res.PaymentLink;
import com.hulkhiretech.payments.paypal.res.PaypalOrder;
import com.hulkhiretech.payments.paypal.res.error.PaypalErrorResponse;
import com.hulkhiretech.payments.pojo.CreateOrderReq;
import com.hulkhiretech.payments.pojo.OrderResponse;
import com.hulkhiretech.payments.service.PaymentValidator;
import com.hulkhiretech.payments.service.TokenService;
import com.hulkhiretech.payments.service.interfaces.PaymentService;
import com.hulkhiretech.payments.util.JsonUtil;
import com.hulkhiretech.payments.util.PaypalOrderUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final JsonUtil jsonUtil;
    private final TokenService tokenService;
    private final HttpServiceEngine httpServiceEngine;
    private final CaptureOrderHelper captureOrderHelper;
    private final CreateOrderHelper createOrderHelper;
    private final PaymentValidator paymentValidator;


    @Override
    public OrderResponse createOrder(CreateOrderReq createOrderReq) {
        log.info("Creating order in Payment Service Impl");

        paymentValidator.validateCreateOrderRequest(createOrderReq);
        log.info("CreateOrderReq validated successfully: {}", createOrderReq);

        String accessToken = tokenService.getAccessToken();
        log.info("Access Token Retrieved: {}", accessToken);

        HttpRequest httpRequest = createOrderHelper.prepareCreateOrderHttpRequest(createOrderReq, accessToken);
        log.info("Prepared HttpRequest for order creation: {}", httpRequest);

        ResponseEntity<String> httpResponse = httpServiceEngine.makeHttpCall(httpRequest);
        log.info("HTTP Response from HttpServiceEngine {}", httpResponse.getBody());

        OrderResponse orderResponse = createOrderHelper.handlePaypalResponse(httpResponse);
        log.info("Final OrderResponse to be returned: {}", orderResponse);

        return orderResponse;
    }

    @Override
    public OrderResponse captureOrder(String orderId) {
        log.info("Capturing order in Payment Service Impl for orderId: {}", orderId);

        String accessToken = tokenService.getAccessToken();
        log.info("Access Token Retrieved: {}", accessToken);

        HttpRequest httpRequest = captureOrderHelper.prepareCaptureOrderHttpRequest(orderId, accessToken);
        log.info("Prepared HttpRequest for order capture: {}", httpRequest);

        ResponseEntity<String> httpResponse = httpServiceEngine.makeHttpCall(httpRequest);
        log.info("HTTP Response from HttpServiceEngine {}", httpResponse);

        OrderResponse orderResponse = captureOrderHelper.handlePaypalCaptureResponse(httpResponse);
        log.info("Final OrderResponse to be returned after capture: {}", orderResponse);

        return orderResponse;
    }

}
