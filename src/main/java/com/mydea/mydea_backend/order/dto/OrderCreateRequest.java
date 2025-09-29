package com.mydea.mydea_backend.order.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class OrderCreateRequest {
    @NotNull
    private Long cartId;

    @NotBlank private String recipientName;
    @NotBlank private String phone;
    @NotBlank private String address1;
    private String address2;
    @NotBlank private String zipcode;

    private String note;
}