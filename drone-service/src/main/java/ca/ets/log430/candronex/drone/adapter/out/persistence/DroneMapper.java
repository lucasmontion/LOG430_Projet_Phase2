package ca.ets.log430.candronex.drone.adapter.out.persistence;

import ca.ets.log430.candronex.commun.domaine.ClientId;
import ca.ets.log430.candronex.drone.domain.*;
import org.springframework.stereotype.Component;

/** Traduit entre le modèle de domaine (Drone) et le modèle de persistance (DroneJpaEntity). */
@Component
public class DroneMapper {

    public DroneJpaEntity versEntite(Drone drone) {
        return new DroneJpaEntity(
                new DroneJpaId(drone.clientId().valeur(), drone.id().valeur()),
                drone.imsi().valeur(),
                drone.iccid().valeur(),
                drone.typeSim().name(),
                drone.statut().name());
    }

    public Drone versDomaine(DroneJpaEntity entite) {
        return Drone.reconstituer(
                new DroneId(entite.getId().getDroneId()),
                new ClientId(entite.getId().getClientId()),
                new Imsi(entite.getImsi()),
                new Iccid(entite.getIccid()),
                TypeSim.valueOf(entite.getTypeSim()),
                StatutDrone.valueOf(entite.getStatut()));
    }
}
