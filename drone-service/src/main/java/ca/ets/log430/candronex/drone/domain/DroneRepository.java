package ca.ets.log430.candronex.drone.domain;

import ca.ets.log430.candronex.commun.domaine.ClientId;

import java.util.Optional;

/**
 * Port de sortie : ce dont le domaine a besoin pour conserver ses drones.
 * Nommé selon le besoin métier, pas selon la technologie.
 * Toute lecture est filtrée par client (isolation entre clients).
 */
public interface DroneRepository {
    void sauvegarder(Drone drone);
    Optional<Drone> trouver(ClientId clientId, DroneId droneId);
    boolean existe(ClientId clientId, DroneId droneId);
    boolean imsiDejaUtilise(Imsi imsi);
    boolean iccidDejaUtilise(Iccid iccid);
}
