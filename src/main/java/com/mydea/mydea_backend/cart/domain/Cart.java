package com.mydea.mydea_backend.cart.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "cart", indexes = {
        @Index(name = "ux_carts_user_active", columnList = "user_id, active_flag", unique = true)
})
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cartId;

    @Column(name = "user_id")
    private Long userId;

    /** 가상 장바구니 여부 */
    @Column(name = "is_virtual", nullable = false)
    private boolean virtualCart; // DB tinyint(1)

    /** 만료 시각 (가상 카트에서만 사용, 정규 카트는 NULL) */
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    /** 생성 시각 */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * 정규 cart 이면 1, 가상이면 NULL
     */
    @Column(name = "active_flag", insertable = false, updatable = false)
    private Integer activeFlag;

    public boolean isExpired() {
        return virtualCart && expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
    }
}