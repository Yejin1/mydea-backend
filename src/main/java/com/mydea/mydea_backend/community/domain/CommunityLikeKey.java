package com.mydea.mydea_backend.community.domain;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommunityLikeKey implements Serializable {
    private Long postId;
    private Long userId;
}
