package com.hulkhiretech.payments.http;

import com.hulkhiretech.payments.constant.ErrorCodeEnum;
import com.hulkhiretech.payments.exception.PaypalProviderException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import java.util.function.Consumer;

@Component
@Slf4j
@RequiredArgsConstructor
public class HttpServiceEngine {

    private final RestClient restClient;

    public ResponseEntity<String> makeHttpCall(HttpRequest httpRequest) {
        log.info("making Http call in Http Service Engine");

        try {
            ResponseEntity<String> httpResponse = restClient.method(httpRequest.getHttpMethod())
                    .uri(httpRequest.getUrl())
                    .headers(restClientHeaders -> restClientHeaders.addAll(httpRequest.getHttpHeaders()))
                    .body(httpRequest.getBody())
                    .retrieve()
                    .toEntity(String.class);
            log.info("HTTP call completed, HttpResponse {}", httpResponse);
            return httpResponse;
        }
        catch(HttpClientErrorException | HttpServerErrorException e){
            log.error("HTTP error response received: {}", e.getMessage(), e);
            //if error is gateway timeout or service unavailable then throw service unavailable exception

            if(e.getStatusCode()==HttpStatus.GATEWAY_TIMEOUT ||
               e.getStatusCode()==HttpStatus.SERVICE_UNAVAILABLE){
                throw new PaypalProviderException(
                        ErrorCodeEnum.PAYPAL_SERVICE_UNAVAILABLE.getErrorCode(),
                        ErrorCodeEnum.PAYPAL_SERVICE_UNAVAILABLE.getErrorMessage(),
                        HttpStatus.SERVICE_UNAVAILABLE);
            }

            String responseBodyAsString = e.getResponseBodyAsString();
            log.error("Response Body: {}", responseBodyAsString);

            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(responseBodyAsString);

        }catch (Exception e) {
            log.error("Exception while preparing form data: {}", e.getMessage(), e);
            throw new PaypalProviderException(
                    ErrorCodeEnum.PAYPAL_SERVICE_UNAVAILABLE.getErrorCode(),
                    ErrorCodeEnum.PAYPAL_SERVICE_UNAVAILABLE.getErrorMessage(),
                    HttpStatus.SERVICE_UNAVAILABLE);
        }

    }
}
