package com.mydea.mydea_backend.community.dto;

import com.mydea.mydea_backend.community.domain.CommunityVisibility;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PostUpdateRequest {
    @Nullable
    private CommunityVisibility visibility; // PUBLIC/UNLISTED/PRIVATE

    @Nullable
    private Boolean allowCopy;

    @Nullable
    private Boolean allowComments;

    @Size(max = 200)
    private String title;

    private String description;
}
