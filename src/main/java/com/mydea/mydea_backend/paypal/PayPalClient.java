package com.mydea.mydea_backend.paypal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PayPalClient {
    private final WebClient web;
    private final String clientId;
    private final String clientSecret;

    public PayPalClient(
            @Value("${PAYPAL_BASE}") String base,
            @Value("${PAYPAL_CLIENT_ID}") String clientId,
            @Value("${PAYPAL_CLIENT_SECRET}") String clientSecret
    ) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.web = WebClient.builder()
                .baseUrl(base)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

    }

    private Mono<String> getAccessToken() {
        return web.post()
                .uri("/v1/oauth2/token")
                .headers(h -> {
                    h.setBasicAuth(clientId, clientSecret);
                    h.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                })
                .body(BodyInserters.fromFormData("grant_type", "client_credentials"))
                .retrieve()
                .onStatus(s -> !s.is2xxSuccessful(), resp ->
                        resp.bodyToMono(String.class).flatMap(body -> {
                            log.error("PayPal token error: status={} body={}", resp.statusCode(), body);
                            return Mono.error(new IllegalStateException("paypal_token_error"));
                        })
                )
                .bodyToMono(MapToken.class)
                .map(MapToken::access_token);
    }

    public Mono<String> createOrder(String referenceId, String amount, String currency) {
        String payload = """
        {
          "intent": "CAPTURE",
          "purchase_units": [{
            "reference_id": "%s",
            "amount": { "currency_code": "%s", "value": "%s" }
          }]
        }
        """.formatted(referenceId, currency, amount);

        return getAccessToken().flatMap(tok ->
                web.post()
                        .uri("/v2/checkout/orders")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tok)
                        .bodyValue(payload)
                        .retrieve()
                        .onStatus(s -> !s.is2xxSuccessful(), resp ->
                                resp.bodyToMono(String.class).flatMap(body -> {
                                    log.error("PayPal createOrder error: status={} body={}", resp.statusCode(), body);
                                    return Mono.error(new IllegalStateException("paypal_create_order_error"));
                                })
                        )
                        .bodyToMono(String.class)
        );
    }

    public Mono<String> captureOrder(String orderId) {
        return getAccessToken().flatMap(tok ->
                web.post()
                        .uri("/v2/checkout/orders/{id}/capture", orderId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tok)
                        .retrieve()
                        .onStatus(s -> !s.is2xxSuccessful(), resp ->
                                resp.bodyToMono(String.class).flatMap(body -> {
                                    log.error("PayPal captureOrder error: status={} body={}", resp.statusCode(), body);
                                    return Mono.error(new IllegalStateException("paypal_capture_error"));
                                })
                        )
                        .bodyToMono(String.class)
        );
    }

    public record MapToken(String access_token, String token_type, long expires_in) {}
}
