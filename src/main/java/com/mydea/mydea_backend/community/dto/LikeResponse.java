package com.mydea.mydea_backend.community.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LikeResponse {
    private boolean liked;
    private long likeCount;
}
