package com.hulkhiretech.payments.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hulkhiretech.payments.constant.Constant;
import com.hulkhiretech.payments.http.HttpRequest;
import com.hulkhiretech.payments.http.HttpServiceEngine;
import com.hulkhiretech.payments.paypal.res.PaypalOAuthToken;
import com.hulkhiretech.payments.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;


@Service
@Slf4j
@RequiredArgsConstructor
public class TokenService {

    private final HttpServiceEngine httpServiceEngine;

    private static String accessToken;
    @Value("${paypal.client.id}")
    private String clientId;
    @Value("${paypal.client.secret}")
    private String clientSecret;
    @Value("${paypal.oauth.url}")
    private String oAuthurl;

    private final JsonUtil jsonUtil;

    public String getAccessToken() {
        log.info("Retrieving access token from TokenService");
        if (accessToken != null) {
            log.info("Returning cached access token");
            return accessToken;
        }
        log.info("No cached access token found, calling OAuth service");

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBasicAuth(clientId, clientSecret);
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add(Constant.GRANT_TYPE, Constant.CLIENT_CREDENTIALS);
        HttpRequest httpRequest = new HttpRequest();
        httpRequest.setHttpMethod(HttpMethod.POST);
        httpRequest.setHttpHeaders(httpHeaders);
        httpRequest.setBody(formData);

        httpRequest.setUrl(oAuthurl);
        ResponseEntity<String> response = httpServiceEngine.makeHttpCall(httpRequest);

        log.info("HTTP response from Http service Engine: {}", response);

        String tokenBody = response.getBody();
        log.info("Access token response body: {}", tokenBody);

        PaypalOAuthToken token =jsonUtil.fromJson(tokenBody, PaypalOAuthToken.class);
        accessToken = token.getAccessToken();
        log.info("Caching access token for future use");

        return accessToken;
    }
}
