package ca.ets.log430.candronex.commande.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface CommandeJpaRepository extends JpaRepository<CommandeJpaEntity, UUID> {
    Optional<CommandeJpaEntity> findByIdAndClientId(UUID id, String clientId);
    Optional<CommandeJpaEntity> findByClientIdAndCleIdempotence(String clientId, String cleIdempotence);
}
