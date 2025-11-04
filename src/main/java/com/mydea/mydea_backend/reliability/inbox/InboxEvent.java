package com.mydea.mydea_backend.reliability.inbox;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "inbox_event", uniqueConstraints = @UniqueConstraint(name = "uk_inbox_provider_event", columnNames = {
        "provider", "provider_event_id" }), indexes = @Index(name = "idx_inbox_processed", columnList = "processed_at"))
public class InboxEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inbox_id")
    private Long id;

    @Column(name = "provider", length = 50, nullable = false)
    private String provider;

    @Column(name = "provider_event_id", length = 100, nullable = false)
    private String providerEventId;

    @Column(name = "payload", columnDefinition = "json", nullable = false)
    private String payload;

    @Column(name = "received_at", insertable = false, updatable = false)
    private LocalDateTime receivedAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;
}
