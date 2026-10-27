package ca.ets.log430.candronex.drone.application;

import ca.ets.log430.candronex.commun.domaine.ClientId;
import ca.ets.log430.candronex.drone.domain.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Adaptateur de test branché sur le même port : permet de tester sans base de données. */
class DroneRepositoryEnMemoire implements DroneRepository {
    private final List<Drone> drones = new ArrayList<>();

    @Override
    public void sauvegarder(Drone drone) { drones.add(drone); }

    @Override
    public Optional<Drone> trouver(ClientId clientId, DroneId droneId) {
        return drones.stream()
                .filter(d -> d.appartientA(clientId) && d.id().equals(droneId))
                .findFirst();
    }

    @Override
    public boolean existe(ClientId clientId, DroneId droneId) {
        return trouver(clientId, droneId).isPresent();
    }

    @Override
    public boolean imsiDejaUtilise(Imsi imsi) {
        return drones.stream().anyMatch(d -> d.imsi().equals(imsi));
    }

    @Override
    public boolean iccidDejaUtilise(Iccid iccid) {
        return drones.stream().anyMatch(d -> d.iccid().equals(iccid));
    }
}
