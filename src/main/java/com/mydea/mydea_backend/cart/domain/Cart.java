package com.mydea.mydea_backend.cart.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name="cart")
public class Cart {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long cartId;
    private Long userId;
    @CreationTimestamp
    private LocalDateTime createdAt;
}