package com.mydea.mydea_backend.order.service;
import org.springframework.stereotype.Component;


@Component
public class ShippingCalculator {
    private static final int FREE_THRESHOLD = 50000; // 5만원 이상 무료배송
    private static final int DEFAULT_FEE = 3000;

    public int calcShippingFee(int subtotal) {
        return subtotal >= FREE_THRESHOLD ? 0 : DEFAULT_FEE;
    }
}
