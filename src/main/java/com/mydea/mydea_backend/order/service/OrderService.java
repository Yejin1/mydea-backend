package com.mydea.mydea_backend.order.service;


import com.mydea.mydea_backend.cart.domain.CartItem;
import com.mydea.mydea_backend.cart.repo.CartItemRepository;
import com.mydea.mydea_backend.order.dto.*;
import com.mydea.mydea_backend.order.domain.*;
import com.mydea.mydea_backend.order.repo.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;
    private final OrderEventRepository orderEventRepository;
    private final ShippingCalculator shippingCalculator;

    public OrderPreviewResponse preview(Long cartId) {
        List<CartItem> cartItems = cartItemRepository.findByCartId(cartId);
        if (cartItems.isEmpty()) throw new IllegalStateException("장바구니가 비어있습니다.");
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
        if (idempotencyKey != null) {
            Optional<Order> existed = orderRepository.findByIdempotencyKey(idempotencyKey);
            if (existed.isPresent()) return toResponse(existed.get());
        }

        List<CartItem> cartItems = cartItemRepository.findByCartId(req.getCartId());
        if (cartItems.isEmpty()) throw new IllegalStateException("장바구니가 비어있습니다.");

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

        return toResponse(order);
    }

    @Transactional
    public OrderResponse paySimulator(Long accountId, Long orderId, PayRequest req) {
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

        return toResponse(order);
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

        if (!(order.getStatus() == OrderStatus.PAYMENT_PENDING || order.getStatus() == OrderStatus.CREATED || order.getStatus() == OrderStatus.PAID)) {
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
}