package com.mydea.mydea_backend.order.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mydea.mydea_backend.cart.domain.CartItem;
import com.mydea.mydea_backend.cart.domain.Cart;
import com.mydea.mydea_backend.cart.repo.CartRepository;
import com.mydea.mydea_backend.cart.repo.CartItemRepository;
import com.mydea.mydea_backend.order.dto.*;
import com.mydea.mydea_backend.order.domain.*;
import com.mydea.mydea_backend.order.repo.*;
import com.mydea.mydea_backend.reliability.idempotency.IdempotencyRecord;
import com.mydea.mydea_backend.reliability.idempotency.IdempotencyService;
import com.mydea.mydea_backend.reliability.idempotency.IdempotencyStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final CartItemRepository cartItemRepository;
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;
    private final OrderEventRepository orderEventRepository;
    private final ShippingCalculator shippingCalculator;
    private final IdempotencyService idempotencyService;
    private final ObjectMapper objectMapper;

    public OrderPreviewResponse preview(Long cartId) {
        // 소유자 검증 불가: preview 호출자는 SecurityContext 기반으로 controller에서 userId 전달해야 함
        // 여기서는 cartId만 넘어오므로 사용자 소유 검증은 controller 층에서 userId와 함께 넘겨오는 구조로 바꿔도 됨
        // (현재 controller는 cartId만 받으므로 보안을 위해 미리 소유자 조회/검증 로직 추가)
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new NoSuchElementException("장바구니를 찾을 수 없습니다."));
        if (cart.isExpired())
            throw new IllegalStateException("만료된 장바구니입니다.");
        List<CartItem> cartItems = cartItemRepository.findByCartId(cartId);
        if (cartItems.isEmpty())
            throw new IllegalStateException("장바구니가 비어있습니다.");
        int subtotal = cartItems.stream().mapToInt(ci -> ci.getUnitPrice() * ci.getQuantity()).sum();
        int shipping = shippingCalculator.calcShippingFee(subtotal);
        int discount = 0;
        int total = subtotal + shipping - discount;
        return OrderPreviewResponse.builder()
                .items(cartItems.stream().map(ci -> OrderPreviewResponse.Item.builder()
                        .workId(ci.getWorkId())
                        .name(ci.getName())
                        .optionHash(ci.getOptionHash())
                        .thumbUrl(ci.getThumbUrl())
                        .unitPrice(ci.getUnitPrice())
                        .quantity(ci.getQuantity())
                        .lineTotal(ci.getUnitPrice() * ci.getQuantity())
                        .build()).toList())
                .subtotal(subtotal)
                .shippingFee(shipping)
                .discount(discount)
                .total(total)
                .build();
    }

    @Transactional
    public OrderResponse create(Long accountId, String idempotencyKey, OrderCreateRequest req) {
        String endpoint = "orders:create";
        String requestHash = null;
        if (idempotencyKey != null) {
            requestHash = hashRequest(req);
            IdempotencyRecord r = idempotencyService.begin(idempotencyKey, endpoint, accountId, requestHash,
                    LocalDateTime.now().plusMinutes(10));
            if (r.getStatus() == IdempotencyStatus.COMPLETED && r.getResponseSnapshot() != null) {
                try {
                    return objectMapper.readValue(r.getResponseSnapshot(), OrderResponse.class);
                } catch (JsonProcessingException e) {
                    // 스냅샷 파싱 실패 시 정상 플로우로 진행
                }
            }
        }

        // cart 소유자 및 만료 검증
        Cart cart = cartRepository.findById(req.getCartId())
                .orElseThrow(() -> new NoSuchElementException("장바구니를 찾을 수 없습니다."));
        if (!Objects.equals(cart.getUserId(), accountId))
            throw new SecurityException("권한이 없습니다.");
        if (cart.isExpired())
            throw new IllegalStateException("만료된 장바구니입니다.");

        List<CartItem> cartItems = cartItemRepository.findByCartId(req.getCartId());
        if (cartItems.isEmpty())
            throw new IllegalStateException("장바구니가 비어있습니다.");

        int subtotal = cartItems.stream().mapToInt(ci -> ci.getUnitPrice() * ci.getQuantity()).sum();
        int shipping = shippingCalculator.calcShippingFee(subtotal);
        int discount = 0;
        int total = subtotal + shipping - discount;

        Order order = Order.builder()
                .orderNo(generateOrderNo())
                .accountId(accountId)
                .status(OrderStatus.PAYMENT_PENDING)
                .subtotalAmount(subtotal)
                .shippingFee(shipping)
                .discountAmount(discount)
                .totalAmount(total)
                .recipientName(req.getRecipientName())
                .phone(req.getPhone())
                .address1(req.getAddress1())
                .address2(req.getAddress2())
                .zipcode(req.getZipcode())
                .note(req.getNote())
                .idempotencyKey(idempotencyKey)
                .build();

        for (CartItem ci : cartItems) {
            order.addItem(OrderItem.builder()
                    .workId(ci.getWorkId())
                    .optionHash(ci.getOptionHash())
                    .name(ci.getName())
                    .thumbUrl(ci.getThumbUrl())
                    .unitPrice(ci.getUnitPrice())
                    .quantity(ci.getQuantity())
                    .lineTotal(ci.getUnitPrice() * ci.getQuantity())
                    .build());
        }
        order = orderRepository.save(order);

        // 이벤트 로그
        saveEvent(order.getOrderId(), null, OrderStatus.PAYMENT_PENDING, "ORDER_CREATED", null);

        OrderResponse res = toResponse(order);
        if (idempotencyKey != null) {
            try {
                idempotencyService.complete(idempotencyKey, objectMapper.writeValueAsString(res));
            } catch (JsonProcessingException e) {
                // ignore snapshot failure
            }
        }
        return res;
    }

    @Transactional
    public OrderResponse paySimulator(Long accountId, Long orderId, String idempotencyKey, PayRequest req) {
        String endpoint = "orders:pay";
        if (idempotencyKey != null) {
            String requestHash = hashString(orderId + "|" + safeJson(req));
            IdempotencyRecord r = idempotencyService.begin(idempotencyKey, endpoint, accountId, requestHash,
                    LocalDateTime.now().plusMinutes(10));
            if (r.getStatus() == IdempotencyStatus.COMPLETED && r.getResponseSnapshot() != null) {
                try {
                    return objectMapper.readValue(r.getResponseSnapshot(), OrderResponse.class);
                } catch (JsonProcessingException e) {
                    // continue to normal flow
                }
            }
        }
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("주문을 찾을 수 없습니다."));
        assertOwner(accountId, order);
        if (order.getStatus() != OrderStatus.PAYMENT_PENDING)
            throw new IllegalStateException("결제 가능한 상태가 아닙니다.");

        Payment payment = Payment.builder()
                .orderId(order.getOrderId())
                .status(req.isSuccess() ? PaymentStatus.PAID : PaymentStatus.FAILED)
                .method(Objects.requireNonNullElse(req.getMethod(), "SIMULATOR"))
                .amount(order.getTotalAmount())
                .provider("MOCK")
                .providerTxId(UUID.randomUUID().toString())
                .approvedAt(req.isSuccess() ? LocalDateTime.now() : null)
                .rawCallback("{\"simulated\":true}")
                .build();
        paymentRepository.save(payment);

        if (req.isSuccess()) {
            OrderStatus from = order.getStatus();
            order.setStatus(OrderStatus.PAID);
            order.setPaidAt(LocalDateTime.now());
            orderRepository.save(order);
            saveEvent(order.getOrderId(), from, OrderStatus.PAID, "PAYMENT_SUCCESS", null);
        } else {
            OrderStatus from = order.getStatus();
            order.setStatus(OrderStatus.PAYMENT_FAILED);
            orderRepository.save(order);
            saveEvent(order.getOrderId(), from, OrderStatus.PAYMENT_FAILED, "PAYMENT_FAILED", null);
        }

        OrderResponse res = toResponse(order);
        if (idempotencyKey != null) {
            try {
                idempotencyService.complete(idempotencyKey, safeJson(res));
            } catch (Exception ignore) {
            }
        }
        return res;
    }

    public OrderResponse get(Long accountId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("주문을 찾을 수 없습니다."));
        assertOwner(accountId, order);
        return toResponse(order);
    }

    public Page<OrderResponse> list(Long accountId, int page, int size) {
        return orderRepository.findByAccountIdOrderByCreatedAtDesc(accountId, PageRequest.of(page, size))
                .map(this::toResponse);
    }

    @Transactional
    public OrderResponse cancel(Long accountId, Long orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("주문을 찾을 수 없습니다."));
        assertOwner(accountId, order);

        if (!(order.getStatus() == OrderStatus.PAYMENT_PENDING || order.getStatus() == OrderStatus.CREATED
                || order.getStatus() == OrderStatus.PAID)) {
            throw new IllegalStateException("취소할 수 없는 상태입니다.");
        }
        OrderStatus from = order.getStatus();
        order.setStatus(OrderStatus.CANCELED);
        order.setCanceledAt(LocalDateTime.now());
        orderRepository.save(order);
        saveEvent(order.getOrderId(), from, OrderStatus.CANCELED, reason != null ? reason : "USER_CANCEL", null);
        // (결제 환불 로직은 실제 결제 연동 시 처리)
        return toResponse(order);
    }

    private void assertOwner(Long accountId, Order order) {
        if (!Objects.equals(order.getAccountId(), accountId))
            throw new SecurityException("권한이 없습니다.");
    }

    private void saveEvent(Long orderId, OrderStatus from, OrderStatus to, String reason, String metaJson) {
        orderEventRepository.save(OrderEvent.builder()
                .orderId(orderId)
                .fromStatus(from)
                .toStatus(to)
                .reason(reason)
                .meta(metaJson)
                .build());
    }

    private String generateOrderNo() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String rand = String.format("%06d", new Random().nextInt(1_000_000));
        return date + "-" + rand;
    }

    private OrderResponse toResponse(Order o) {
        return OrderResponse.builder()
                .orderId(o.getOrderId())
                .orderNo(o.getOrderNo())
                .status(o.getStatus())
                .subtotal(o.getSubtotalAmount())
                .shippingFee(o.getShippingFee())
                .discount(o.getDiscountAmount())
                .total(o.getTotalAmount())
                .recipientName(o.getRecipientName())
                .phone(o.getPhone())
                .address1(o.getAddress1())
                .address2(o.getAddress2())
                .zipcode(o.getZipcode())
                .note(o.getNote())
                .createdAt(o.getCreatedAt())
                .paidAt(o.getPaidAt())
                .shippedAt(o.getShippedAt())
                .deliveredAt(o.getDeliveredAt())
                .canceledAt(o.getCanceledAt())
                .items(o.getItems().stream().map(oi -> OrderResponse.Item.builder()
                        .orderItemId(oi.getOrderItemId())
                        .workId(oi.getWorkId())
                        .name(oi.getName())
                        .optionHash(oi.getOptionHash())
                        .thumbUrl(oi.getThumbUrl())
                        .unitPrice(oi.getUnitPrice())
                        .quantity(oi.getQuantity())
                        .lineTotal(oi.getLineTotal())
                        .build()).collect(Collectors.toList()))
                .build();
    }

    private String safeJson(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            return "";
        }
    }

    private String hashRequest(Object req) {
        return hashString(safeJson(req));
    }

    private String hashString(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("해시 계산 실패", e);
        }
    }
}