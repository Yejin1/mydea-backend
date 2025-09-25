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
@Table(name="cart_item",
        uniqueConstraints=@UniqueConstraint(name="uq_cart_work",
                columnNames={"cart_id","work_id","option_hash"}))
public class CartItem {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long cartItemId;

    private Long cartId;
    private Long workId;
    private String optionHash;
    private String name;
    private String thumbUrl;
    private Integer unitPrice;
    private Integer quantity;
    @CreationTimestamp private LocalDateTime addedAt;
}