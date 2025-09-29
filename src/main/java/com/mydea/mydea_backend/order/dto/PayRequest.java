package com.mydea.mydea_backend.order.dto;

import lombok.Data;

@Data
public class PayRequest {
    private boolean success = true;
    private String method = "SIMULATOR";
}