package ca.ets.log430.candronex.commande.adapter.out.persistence;

import ca.ets.log430.candronex.commande.domain.*;
import ca.ets.log430.candronex.commun.domaine.ClientId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Adaptateur de sortie : la commande et ses éléments sont enregistrés
 * dans UNE seule transaction (tout ou rien).
 */
@Repository
public class JpaCommandeRepository implements CommandeRepository {
    private static final Logger log = LoggerFactory.getLogger(JpaCommandeRepository.class);

    private final CommandeJpaRepository jpa;
    private final CommandeMapper mapper;

    JpaCommandeRepository(CommandeJpaRepository jpa, CommandeMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public void sauvegarder(Commande commande) {
        log.info("[adapter.out] JpaCommandeRepository.sauvegarder commandeId={} elements={}",
                commande.id().valeur(), commande.elements().size());
        jpa.save(mapper.versEntite(commande));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Commande> trouver(ClientId clientId, CommandeId commandeId) {
        return jpa.findByIdAndClientId(commandeId.valeur(), clientId.valeur()).map(mapper::versDomaine);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Commande> trouverParCle(ClientId clientId, CleIdempotence cle) {
        return jpa.findByClientIdAndCleIdempotence(clientId.valeur(), cle.valeur()).map(mapper::versDomaine);
    }
}
