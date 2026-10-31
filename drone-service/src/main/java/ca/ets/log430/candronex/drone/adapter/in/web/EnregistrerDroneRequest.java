package ca.ets.log430.candronex.drone.adapter.in.web;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** DTO web reçu par POST /api/drone-service/v1/drone. Le format fin est validé par le domaine. */
@Schema(description = "Drone à enregistrer et son identité réseau")
public record EnregistrerDroneRequest(
        @Schema(description = "Choisi par le client, unique pour ce client", pattern = "^[A-Za-z0-9-]{1,64}$",
                example = "DRONE-600")
        @NotBlank String droneId,
        @Schema(description = "Unique dans tout le système ; jamais renvoyé ni journalisé en clair",
                pattern = "^[0-9]{15}$", example = "\"999700000000001\"")
        @NotBlank String imsi,
        @Schema(description = "Unique dans tout le système ; jamais renvoyé", pattern = "^[0-9]{19,20}$",
                example = "\"8999970000000000101\"")
        @NotBlank String iccid,
        @Schema(allowableValues = {"SIM", "ESIM"}, example = "ESIM")
        @NotBlank String typeSim
) {}
