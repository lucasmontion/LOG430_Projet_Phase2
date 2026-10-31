package ca.ets.log430.candronex.commande.adapter.out.persistence;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Les lectures chargent la commande ET ses éléments en une seule requête (@EntityGraph) :
 * le résultat reste utilisable même hors transaction (ex. relecture après une requête simultanée).
 */
interface CommandeJpaRepository extends JpaRepository<CommandeJpaEntity, UUID> {
    @EntityGraph(attributePaths = "elements")
    Optional<CommandeJpaEntity> findByIdAndClientId(UUID id, String clientId);

    @EntityGraph(attributePaths = "elements")
    Optional<CommandeJpaEntity> findByClientIdAndCleIdempotence(String clientId, String cleIdempotence);
}
