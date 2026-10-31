package ca.ets.log430.candronex.drone.adapter.in.web;

import ca.ets.log430.candronex.drone.domain.Drone;
import io.swagger.v3.oas.annotations.media.Schema;

/** DTO web retourné au client : l'IMSI et l'ICCID n'en font jamais partie. */
@Schema(description = "Drone du client, sans identité réseau")
public record DroneResponse(
        @Schema(example = "DRONE-600") String droneId,
        @Schema(allowableValues = {"SIM", "ESIM"}, example = "ESIM") String typeSim,
        @Schema(allowableValues = {"ACTIF", "SUSPENDU", "RETIRE"}, example = "ACTIF") String statut) {

    public static DroneResponse depuis(Drone drone) {
        return new DroneResponse(drone.id().valeur(), drone.typeSim().name(), drone.statut().name());
    }
}
