package com.mydea.mydea_backend.paypal;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/paypal")
public class PayPalController {
    private final PayPalClient paypal;

    public PayPalController(PayPalClient paypal) { this.paypal = paypal; }

    @PostMapping("/orders")
    public Mono<ResponseEntity<String>> create(@RequestParam String ref,
                                               @RequestParam(defaultValue = "KRW") String currency,
                                               @RequestParam String amount) {
        return paypal.createOrder(ref, amount, currency)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/orders/{orderId}/capture")
    public Mono<ResponseEntity<String>> capture(@PathVariable String orderId) {
        return paypal.captureOrder(orderId).map(ResponseEntity::ok);
    }
}
