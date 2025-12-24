package com.hulkhiretech.payments.helper;

import com.hulkhiretech.payments.constant.Constant;
import com.hulkhiretech.payments.constant.ErrorCodeEnum;
import com.hulkhiretech.payments.exception.PaypalProviderException;
import com.hulkhiretech.payments.http.HttpRequest;
import com.hulkhiretech.payments.paypal.res.PaypalOrder;
import com.hulkhiretech.payments.paypal.res.error.PaypalErrorResponse;
import com.hulkhiretech.payments.pojo.OrderResponse;
import com.hulkhiretech.payments.util.JsonUtil;
import com.hulkhiretech.payments.util.PaypalOrderUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class CaptureOrderHelper {

    private final JsonUtil jsonUtil;

    private static final String ORDER_ID_REF = "{orderId}";

    @Value("${paypal.capture.order.url}")
    private String captureOrderUrlTemplate;

    public HttpRequest prepareCaptureOrderHttpRequest(String orderId, String accessToken) {
        log.info("Preparing capture order HttpRequest"
                + " || Order ID: {},access token: {}", orderId, accessToken);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        //set header paypal-request-id => UUID
        String uuid = UUID.randomUUID().toString();
        log.info("Generated UUID for paypal-request-id header: {}", uuid);
        headers.add(Constant.PAY_PAL_REQUEST_ID, uuid);

        String requestAsJson = "";

        String captureOrderUrl = captureOrderUrlTemplate.replace(ORDER_ID_REF, orderId);
        log.info("Prepared capture Order URL: {}", captureOrderUrl);

        HttpRequest httpRequest = new HttpRequest();
        httpRequest.setHttpMethod(HttpMethod.POST);
        httpRequest.setUrl(captureOrderUrl);
        httpRequest.setHttpHeaders(headers);
        httpRequest.setBody(requestAsJson);

        log.info("Prepared HttpRequest for Capture Order: {}", httpRequest);
        return httpRequest;
    }


    public OrderResponse handlePaypalCaptureResponse(ResponseEntity<String> httpResponse) {
        log.info("Handling PayPal capture order response in PaymentServiceImpl" + "httpResponse:{}", httpResponse);
        if (httpResponse.getStatusCode().is2xxSuccessful()) {
            PaypalOrder paypalOrder = jsonUtil.fromJson(httpResponse.getBody(), PaypalOrder.class);
            log.info("Converted PayPal capture order response to PaypalOrder object: {}", paypalOrder);

            OrderResponse orderResponse = toOrderResponse(paypalOrder);
            log.info("Converted PaypalOrder to OrderResponse object: {}", orderResponse);

            if (orderResponse != null
                    && orderResponse.getOrderId() != null
                    && !orderResponse.getOrderId().isBlank()
                    && orderResponse.getPaypalStatus() != null
                    && !orderResponse.getPaypalStatus().isBlank()) {
                log.info("Order capture successful || orderResponse: {}", orderResponse);
                return orderResponse;

            }
            log.error("Order creation failed or incomplete details received from PayPal"
                    + " || orderResponse: {}", orderResponse);

        }

        // if 4xx or 5xx then proper error
        if (httpResponse.getStatusCode().is4xxClientError()
                || httpResponse.getStatusCode().is5xxServerError()) {
            log.error("Received 4xx, 5xx error response from PayPal service");

            PaypalErrorResponse paypalErrorRes = jsonUtil.fromJson(
                    httpResponse.getBody(), PaypalErrorResponse.class);
            log.info("PayPal error response details: {}", paypalErrorRes);

            String errorCode = ErrorCodeEnum.PAYPAL_ERROR.getErrorCode();
            String errorMessage = PaypalOrderUtil.getPaypalErrorSummary(
                    paypalErrorRes);
            log.info("Generated PayPal error summary: {}", errorMessage);

            throw new PaypalProviderException(
                    errorCode,
                    errorMessage,
                    HttpStatus.valueOf(
                            httpResponse.getStatusCode().value()));
        }

        log.error("Unexpected response from PayPal service. "
                + "httpResponse: {}", httpResponse);

        throw new PaypalProviderException(
                ErrorCodeEnum.PAYPAL_UNKNOWN_ERROR.getErrorCode(),
                ErrorCodeEnum.PAYPAL_UNKNOWN_ERROR.getErrorMessage(),
                HttpStatus.BAD_GATEWAY);
    }

    private OrderResponse toOrderResponse(PaypalOrder paypalOrder) {
        log.info("Converting PaypalOrder to OrderResponse: {}", paypalOrder);

        OrderResponse orderResponse = new OrderResponse();
        orderResponse.setOrderId(paypalOrder.getId());
        orderResponse.setPaypalStatus(paypalOrder.getStatus());

        log.info("Converted OrderResponse: {}", orderResponse);
        return orderResponse;
    }
}
