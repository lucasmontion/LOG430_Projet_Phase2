package ca.ets.log430.candronex.drone.adapter.in.web;

import ca.ets.log430.candronex.drone.domain.Drone;

/** DTO web retourné au client : l'IMSI et l'ICCID n'en font jamais partie. */
public record DroneResponse(String droneId, String typeSim, String statut) {
    public static DroneResponse depuis(Drone drone) {
        return new DroneResponse(drone.id().valeur(), drone.typeSim().name(), drone.statut().name());
    }
}
