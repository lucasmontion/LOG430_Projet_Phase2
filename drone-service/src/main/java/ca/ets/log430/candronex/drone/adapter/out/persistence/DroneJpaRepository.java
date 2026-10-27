package ca.ets.log430.candronex.drone.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

/** Dépôt Spring Data : détail technique, invisible du domaine. */
interface DroneJpaRepository extends JpaRepository<DroneJpaEntity, DroneJpaId> {
    boolean existsByImsi(String imsi);
    boolean existsByIccid(String iccid);
}
