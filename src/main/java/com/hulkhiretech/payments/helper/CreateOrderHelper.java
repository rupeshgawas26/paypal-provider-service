package com.hulkhiretech.payments.helper;

import com.hulkhiretech.payments.constant.Constant;
import com.hulkhiretech.payments.constant.ErrorCodeEnum;
import com.hulkhiretech.payments.exception.PaypalProviderException;
import com.hulkhiretech.payments.http.HttpRequest;
import com.hulkhiretech.payments.paypal.req.*;
import com.hulkhiretech.payments.paypal.res.PaymentLink;
import com.hulkhiretech.payments.paypal.res.PaypalOrder;
import com.hulkhiretech.payments.paypal.res.error.PaypalErrorResponse;
import com.hulkhiretech.payments.pojo.CreateOrderReq;
import com.hulkhiretech.payments.pojo.OrderResponse;
import com.hulkhiretech.payments.util.JsonUtil;
import com.hulkhiretech.payments.util.PaypalOrderUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.UUID;

import static com.hulkhiretech.payments.constant.Constant.PAYER_ACTION_REQUIRED;

@Service
@Slf4j
@RequiredArgsConstructor
public class CreateOrderHelper {

    private final JsonUtil jsonUtil;

    @Value("${paypal.create.order.url}")
    private String createOrderUrl;

    public HttpRequest prepareCreateOrderHttpRequest(CreateOrderReq createOrderReq, String accessToken) {
        HttpHeaders httpHeaders = prepareHeaders(accessToken);

        ExperienceContext context = new ExperienceContext();
        context.setPaymentMethodPreference(Constant.IMMEDIATE_PAYMENT_REQUIRED);
        context.setLandingPage(Constant.LANDING_PAGE_LOGIN);
        context.setShippingPreference(Constant.SHIPPING_PREF_NO_SHIP);
        context.setUserAction(Constant.USER_ACTION_PAY_NOW);
        context.setReturnUrl(createOrderReq.getReturnUrl());
        context.setCancelUrl(createOrderReq.getCancelUrl());

        // PayPal
        PayPal payPal = new PayPal();
        payPal.setExperienceContext(context);

        // PaymentSource
        PaymentSource paymentSource = new PaymentSource();
        paymentSource.setPaypal(payPal);

        // Amount
        Amount amount = new Amount();
        amount.setCurrencyCode(createOrderReq.getCurrencyCode());
        //read amount from createOrderReq and convert to 2 decimal places
        String amountValue = String.format(Constant.TWO_DECIMAL_FORMAT, createOrderReq.getAmount());
        amount.setValue(amountValue);

        // PurchaseUnit
        PurchaseUnit purchaseUnit = new PurchaseUnit();
        purchaseUnit.setAmount(amount);

        // 2. Build the main PaymentRequest object
        OrderRequest order = new OrderRequest();
        order.setIntent(Constant.INTENT_CAPTURE);
        // Use Collections.singletonList for a list with one item
        order.setPurchaseUnits(Collections.singletonList(purchaseUnit));
        order.setPaymentSource(paymentSource);

        String requestAsJson = jsonUtil.toJson(order);

        HttpRequest httpRequest = new HttpRequest();
        httpRequest.setHttpMethod(HttpMethod.POST);
        httpRequest.setUrl(createOrderUrl);
        httpRequest.setHttpHeaders(httpHeaders);
        httpRequest.setBody(requestAsJson);
        return httpRequest;
    }

    private static HttpHeaders prepareHeaders(String accessToken) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBearerAuth(accessToken);

        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        String uuid = UUID.randomUUID().toString();
        httpHeaders.add(Constant.PAY_PAL_REQUEST_ID, uuid);
        return httpHeaders;
    }

    public OrderResponse toOrderResponse(PaypalOrder paypalOrder) {
        OrderResponse orderResponse = new OrderResponse();
        orderResponse.setOrderId(paypalOrder.getId());
        orderResponse.setPaypalStatus(paypalOrder.getStatus());
        String redirectLink = paypalOrder.getLinks().stream()
                .filter(link -> "payer-action".equalsIgnoreCase(link.getRel()))
                .findFirst()
                .map(PaymentLink::getHref)
                .orElse(null);
        orderResponse.setRedirectUrl(redirectLink);
        return orderResponse;
    }

    public OrderResponse handlePaypalResponse(ResponseEntity<String> httpResponse) {
        log.info("Handling Paypal response in Payment Service Impl " + "httpResponse: {}", httpResponse);
        OrderResponse orderResponse =null;

        if (httpResponse.getStatusCode().is2xxSuccessful()) {
            PaypalOrder paypalOrder = jsonUtil.fromJson(httpResponse.getBody(), PaypalOrder.class);
            log.info("Converting HttpResponse to Paypal Order:{}", paypalOrder);

            orderResponse = toOrderResponse(paypalOrder);
            log.info("Converted Paypal order to OrderResponse: {}", orderResponse);
            if (orderResponse != null
                    && orderResponse.getRedirectUrl() != null
                    && !orderResponse.getRedirectUrl().isBlank()
                    && orderResponse.getOrderId() != null
                    && !orderResponse.getOrderId().isBlank()
                    && orderResponse.getPaypalStatus() != null
                    && orderResponse.getPaypalStatus().equalsIgnoreCase(PAYER_ACTION_REQUIRED)) {
                log.info("Order created successfully with PAYER_ACTION_REQUIRED status.");
                return orderResponse;
            }
        }

        log.error("Order creation failed or incomplete data in response: {}", orderResponse);
        //if 4xx or 5xx then proper error handling
        if (httpResponse.getStatusCode().is4xxClientError() ||
                httpResponse.getStatusCode().is5xxServerError()) {
            log.error("Received 4xx,5xx error response from PayPal service.");

            PaypalErrorResponse paypalErrorRes = jsonUtil.fromJson(httpResponse.getBody(), PaypalErrorResponse.class);
            log.info("Paypal error response details: {}", paypalErrorRes);

            String errorCode= ErrorCodeEnum.PAYPAL_ERROR.getErrorCode();
            String errorMessage= PaypalOrderUtil.getPaypalErrorSummary(paypalErrorRes);
            log.info("Generated PayPal error summary: Code - {}, Message - {}", errorCode, errorMessage);

            throw new PaypalProviderException(errorCode,
                    errorMessage,
                    HttpStatus.valueOf(httpResponse.getStatusCode().value()));
        }

        log.error("Unexpected response from PayPal service."+" HttpResponse: {}", httpResponse);

        throw new PaypalProviderException(
                ErrorCodeEnum.PAYPAL_UNKNOWN_ERROR.getErrorCode(),
                ErrorCodeEnum.PAYPAL_UNKNOWN_ERROR.getErrorMessage(),
                HttpStatus.BAD_GATEWAY
        );
    }
}
