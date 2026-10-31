package ca.ets.log430.candronex.drone.adapter.in.web;

import ca.ets.log430.candronex.drone.domain.Drone;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO de la route interne identite-reseau, destinée à slice-service seulement (UC-06).
 * Contient l'IMSI : ne doit jamais être exposé par la passerelle.
 */
@Schema(description = "Identité réseau du drone (route interne seulement)")
public record IdentiteReseauResponse(
        @Schema(example = "DRONE-600") String droneId,
        @Schema(example = "\"999700000000001\"") String imsi,
        @Schema(allowableValues = {"SIM", "ESIM"}, example = "ESIM") String typeSim,
        @Schema(allowableValues = {"ACTIF", "SUSPENDU", "RETIRE"}, example = "ACTIF") String statut) {

    public static IdentiteReseauResponse depuis(Drone drone) {
        return new IdentiteReseauResponse(drone.id().valeur(), drone.imsi().valeur(),
                drone.typeSim().name(), drone.statut().name());
    }
}
