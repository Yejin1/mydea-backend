package com.mydea.mydea_backend.order.domain;

public enum OrderStatus {
    CREATED, PAYMENT_PENDING, PAID, PROCESSING, PACKED, SHIPPED, DELIVERED, COMPLETED,
    CANCELED, EXPIRED, PAYMENT_FAILED
}
