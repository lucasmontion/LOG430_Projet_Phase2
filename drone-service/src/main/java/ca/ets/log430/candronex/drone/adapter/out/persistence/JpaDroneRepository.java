package ca.ets.log430.candronex.drone.adapter.out.persistence;

import ca.ets.log430.candronex.commun.domaine.ClientId;
import ca.ets.log430.candronex.drone.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/** Adaptateur de sortie : implémente le port DroneRepository avec JPA et PostgreSQL. */
@Repository
public class JpaDroneRepository implements DroneRepository {
    private static final Logger log = LoggerFactory.getLogger(JpaDroneRepository.class);

    private final DroneJpaRepository jpa;
    private final DroneMapper mapper;

    JpaDroneRepository(DroneJpaRepository jpa, DroneMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public void sauvegarder(Drone drone) {
        log.info("[adapter.out] JpaDroneRepository.sauvegarder droneId={}", drone.id().valeur());
        jpa.save(mapper.versEntite(drone));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Drone> trouver(ClientId clientId, DroneId droneId) {
        return jpa.findById(new DroneJpaId(clientId.valeur(), droneId.valeur())).map(mapper::versDomaine);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existe(ClientId clientId, DroneId droneId) {
        return jpa.existsById(new DroneJpaId(clientId.valeur(), droneId.valeur()));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean imsiDejaUtilise(Imsi imsi) {
        return jpa.existsByImsi(imsi.valeur());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean iccidDejaUtilise(Iccid iccid) {
        return jpa.existsByIccid(iccid.valeur());
    }
}
