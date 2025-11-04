package com.mydea.mydea_backend.reliability.inbox;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InboxEventRepository extends JpaRepository<InboxEvent, Long> {
    Optional<InboxEvent> findByProviderAndProviderEventId(String provider, String providerEventId);
}
