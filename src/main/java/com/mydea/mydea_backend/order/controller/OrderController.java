package com.mydea.mydea_backend.order.controller;

import com.mydea.mydea_backend.order.dto.*;
import com.mydea.mydea_backend.cart.service.CartService;
import com.mydea.mydea_backend.order.service.OrderService;
import com.mydea.mydea_backend.order.support.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

//주문 API
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final SecurityUtils securityUtils;
    private final CartService cartService;

    // 주문사항 미리보기 데이터 가져오기
    @PostMapping("/preview")
    public ResponseEntity<OrderPreviewResponse> preview(@Valid @RequestBody OrderPreviewRequest req) {
        return ResponseEntity.ok(orderService.preview(req.getCartId()));
    }

    // 주문 생성
    @PostMapping
    public ResponseEntity<OrderResponse> create(Authentication auth,
            @RequestHeader(value = "Idempotency-Key", required = false) String idemKey,
            @Valid @RequestBody OrderCreateRequest req) {
        Long userId = Long.valueOf(auth.getName());
        return ResponseEntity.ok(orderService.create(userId, idemKey, req));
    }

    // 바로 주문 (virtual cart 생성 후 기존 create 로 위임)
    @PostMapping("/direct")
    public ResponseEntity<OrderResponse> direct(Authentication auth,
            @RequestHeader(value = "Idempotency-Key", required = false) String idemKey,
            @Valid @RequestBody DirectOrderRequest req) {
        Long userId = Long.valueOf(auth.getName());
        final long DEFAULT_TTL = 600L; // 10분 기본 유효기간
        Long ttl = req.getTtlSeconds() != null ? req.getTtlSeconds() : DEFAULT_TTL;

        // virtual cart 생성
        Long cartId = cartService.createVirtualCart(userId, req.getItems(), ttl);

        // 기존 주문 생성 DTO 로 매핑
        OrderCreateRequest createReq = new OrderCreateRequest();
        createReq.setCartId(cartId);
        createReq.setRecipientName(req.getRecipientName());
        createReq.setPhone(req.getPhone());
        createReq.setAddress1(req.getAddress1());
        createReq.setAddress2(req.getAddress2());
        createReq.setZipcode(req.getZipcode());
        createReq.setNote(req.getNote());

        return ResponseEntity.ok(orderService.create(userId, idemKey, createReq));
    }

    // 결제진행
    @PostMapping("/{orderId}/pay")
    public ResponseEntity<OrderResponse> pay(Authentication auth, @PathVariable Long orderId,
            @RequestBody PayRequest req) {
        Long userId = Long.valueOf(auth.getName());
        return ResponseEntity.ok(orderService.paySimulator(userId, orderId, req));
    }

    // 주문 상세 내역 보기
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> get(Authentication auth, @PathVariable Long orderId) {
        Long userId = Long.valueOf(auth.getName());
        return ResponseEntity.ok(orderService.get(userId, orderId));
    }

    // 주문 내역
    @GetMapping
    public ResponseEntity<Page<OrderResponse>> list(Authentication auth, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = Long.valueOf(auth.getName());
        return ResponseEntity.ok(orderService.list(userId, page, size));
    }

    // 주문 취소
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancel(Authentication auth, @PathVariable Long orderId,
            @RequestBody CancelRequest req) {
        Long userId = Long.valueOf(auth.getName());
        return ResponseEntity.ok(orderService.cancel(userId, orderId, req != null ? req.getReason() : null));
    }
}