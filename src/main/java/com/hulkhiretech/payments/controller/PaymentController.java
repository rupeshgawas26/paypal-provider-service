package com.hulkhiretech.payments.controller;


import com.hulkhiretech.payments.pojo.CreateOrderReq;
import com.hulkhiretech.payments.pojo.OrderResponse;
import com.hulkhiretech.payments.service.impl.PaymentServiceImpl;
import com.hulkhiretech.payments.service.interfaces.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/orders")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public OrderResponse createOrder(@RequestBody CreateOrderReq createOrderReq){
        log.info("Creating Order in Paypal Provider Service");
        OrderResponse response= paymentService.createOrder(createOrderReq);
        log.info("Order creation response from service is {}",response);
        return response;
    }

    @PostMapping("/{orderId}/capture")
    public OrderResponse captureOrder(@PathVariable String orderId){
        log.info("Capturing Order in Paypal Provider Service for orderId: {}", orderId);
        OrderResponse response= paymentService.captureOrder(orderId);
        log.info("Order capture response from service is {}",response);
        return response;
    }

}
