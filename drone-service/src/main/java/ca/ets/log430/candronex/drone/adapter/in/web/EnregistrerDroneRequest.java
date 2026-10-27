package ca.ets.log430.candronex.drone.adapter.in.web;

import jakarta.validation.constraints.NotBlank;

/** DTO web reçu par POST /api/v1/drones. Le format fin est validé par le domaine. */
public record EnregistrerDroneRequest(
        @NotBlank String droneId,
        @NotBlank String imsi,
        @NotBlank String iccid,
        @NotBlank String typeSim
) {}
