package ca.ets.log430.candronex.slice.adapter.out.persistence;

import ca.ets.log430.candronex.commun.domaine.ClientId;
import ca.ets.log430.candronex.slice.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Adaptateur de sortie : implémente le port ServiceRepository avec JPA et PostgreSQL. */
@Repository
public class JpaServiceRepository implements ServiceRepository {
    private static final Logger log = LoggerFactory.getLogger(JpaServiceRepository.class);

    private final ServiceJpaRepository jpa;
    private final ServiceMapper mapper;

    JpaServiceRepository(ServiceJpaRepository jpa, ServiceMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    /**
     * Volontairement SANS @Transactional : saveAndFlush s'exécute dans sa propre transaction.
     * Si deux demandes identiques arrivent en même temps, la contrainte unique (commande_id, element_id)
     * refuse la seconde ; on relit alors, dans une nouvelle transaction, le service gagnant.
     */
    @Override
    public Service enregistrerSiAbsent(Service service) {
        try {
            jpa.saveAndFlush(mapper.versEntite(service));
            log.info("[adapter.out] JpaServiceRepository.enregistrer serviceId={} etat={}",
                    service.id().valeur(), service.etat());
            return service;
        } catch (DataIntegrityViolationException e) {
            log.info("[adapter.out] demande concurrente pour le même élément : service existant conservé");
            return trouverParOrigine(service.origine()).orElseThrow(() -> e);
        }
    }

    @Override
    @Transactional
    public void sauvegarder(Service service) {
        jpa.save(mapper.versEntite(service));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Service> trouver(ServiceId id) {
        return jpa.findById(id.valeur()).map(mapper::versDomaine);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Service> trouverParOrigine(OrigineCommande origine) {
        return jpa.findByCommandeIdAndElementId(origine.commandeId(), origine.elementId()).map(mapper::versDomaine);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Service> trouverPourClient(ClientId clientId, ServiceId id) {
        return jpa.findByIdAndClientId(id.valeur(), clientId.valeur()).map(mapper::versDomaine);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Service> trouverParCommande(ClientId clientId, UUID commandeId) {
        return jpa.findByClientIdAndCommandeIdOrderByAcceptedAtAsc(clientId.valeur(), commandeId).stream()
                .map(mapper::versDomaine)
                .toList();
    }
}
