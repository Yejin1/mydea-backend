package com.mydea.mydea_backend.cart.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record MergeReq(
        @NotEmpty List<@Valid AddItemReq> items
) {}