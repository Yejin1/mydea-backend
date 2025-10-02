package com.mydea.mydea_backend.order.dto;

import com.mydea.mydea_backend.cart.dto.AddItemReq;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.lang.Nullable;

import java.util.List;

/**
 * 바로 주문(장바구니 생성 없이) 요청 DTO
 * items 를 기반으로 내부에서 virtual cart 를 생성한 뒤 기존 create 흐름 재사용
 */
@Data
public class DirectOrderRequest {
    @NotEmpty
    @Valid
    private List<AddItemReq> items; // 구매할 아이템 목록 (스냅샷 데이터 포함)

    // 배송/수령 정보 (OrderCreateRequest 와 동일 구조 - cartId 제외)
    @NotBlank
    private String recipientName;
    @NotBlank
    private String phone;
    @NotBlank
    private String address1;
    private String address2;
    @NotBlank
    private String zipcode;
    private String note;

    // 가상 장바구니 TTL (초). null 이면 기본값을 사용 (controller 내 상수)
    @Positive
    @Nullable
    private Long ttlSeconds;
}
