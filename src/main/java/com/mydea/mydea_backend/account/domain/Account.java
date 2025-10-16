package com.mydea.mydea_backend.account.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "users", indexes = {
        @Index(name = "idx_users_created_at", columnList = "created_at")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_users_login_id", columnNames = { "login_id" })
})
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 로그인용 별도 아이디 (UNIQUE) */
    @NotBlank
    @Size(max = 50)
    @Column(name = "login_id", length = 50, nullable = false)
    private String loginId;

    @Email
    @Size(max = 255)
    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified;

    @NotBlank
    @Size(max = 255)
    @Column(name = "password", length = 255, nullable = false)
    private String password;

    @Size(max = 100)
    @Column(name = "name", length = 100)
    private String name;

    @Size(max = 50)
    @Column(name = "nickname", length = 50)
    private String nickname;

    @Size(max = 30)
    @Column(name = "phone", length = 30)
    private String phone;

    @Column(name = "phone_verified", nullable = false)
    private boolean phoneVerified;

    /** 권한: USER / ADMIN */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 20, nullable = false)
    private Role role;

    /** 계정상태: ACTIVE / SUSPENDED / DELETED 등 */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private AccountStatus status;

    /** 마지막 로그인 시각 */
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** 사용자 1:N 주소 */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Address> addresses = new ArrayList<>();

    /** 편의 메서드 */
    public void addAddress(Address address) {
        addresses.add(address);
        address.setUser(this);
    }

    public void removeAddress(Address address) {
        addresses.remove(address);
        address.setUser(null);
    }

    /** 로그인 성공 시점 갱신 편의 메서드 */
    public void touchLastLoginAt() {
        this.lastLoginAt = LocalDateTime.now();
    }

    /** 기본값 세팅 */
    @PrePersist
    void prePersist() {
        if (this.role == null)
            this.role = Role.USER;
        if (this.status == null)
            this.status = AccountStatus.ACTIVE;
    }
}
