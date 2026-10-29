package ca.ets.log430.candronex.slice.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface ServiceJpaRepository extends JpaRepository<ServiceJpaEntity, UUID> {
    Optional<ServiceJpaEntity> findByCommandeIdAndElementId(UUID commandeId, UUID elementId);
    Optional<ServiceJpaEntity> findByIdAndClientId(UUID id, String clientId);
    List<ServiceJpaEntity> findByClientIdAndCommandeIdOrderByAcceptedAtAsc(String clientId, UUID commandeId);
}
