package com.mydea.mydea_backend.order.controller;


import com.mydea.mydea_backend.order.dto.*;
import com.mydea.mydea_backend.order.service.OrderService;
import com.mydea.mydea_backend.order.support.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final SecurityUtils securityUtils;

    @PostMapping("/preview")
    public ResponseEntity<OrderPreviewResponse> preview(@Valid @RequestBody OrderPreviewRequest req) {
        return ResponseEntity.ok(orderService.preview(req.getCartId()));
    }

    @PostMapping
    public ResponseEntity<OrderResponse> create(Authentication auth,
            @RequestHeader(value = "Idempotency-Key", required = false) String idemKey,
                                                @Valid @RequestBody OrderCreateRequest req) {
        Long userId = Long.valueOf(auth.getName());
        return ResponseEntity.ok(orderService.create(userId, idemKey, req));
    }

    @PostMapping("/{orderId}/pay")
    public ResponseEntity<OrderResponse> pay(Authentication auth, @PathVariable Long orderId, @RequestBody PayRequest req) {
        Long userId = Long.valueOf(auth.getName());
        return ResponseEntity.ok(orderService.paySimulator(userId, orderId, req));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> get(Authentication auth, @PathVariable Long orderId) {
        Long userId = Long.valueOf(auth.getName());
        return ResponseEntity.ok(orderService.get(userId, orderId));
    }

    @GetMapping
    public ResponseEntity<Page<OrderResponse>> list(Authentication auth, @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "20") int size) {
        Long userId = Long.valueOf(auth.getName());
        return ResponseEntity.ok(orderService.list(userId, page, size));
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancel(Authentication auth, @PathVariable Long orderId, @RequestBody CancelRequest req) {
        Long userId = Long.valueOf(auth.getName());
        return ResponseEntity.ok(orderService.cancel(userId, orderId, req != null ? req.getReason() : null));
    }
}