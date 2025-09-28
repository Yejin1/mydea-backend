package com.mydea.mydea_backend.order.domain;

public enum PaymentStatus {
    NOT_REQUIRED, INITIATED, PENDING, AUTHORIZED, PAID, PARTIAL_REFUNDED, REFUND_PENDING, REFUNDED, FAILED
}