package ca.ets.log430.candronex.slice.adapter.in.web;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

/** DTO reçu par POST /api/slice-service/v1/activation (appelé par commande-service seulement). */
@Schema(description = "Élément de commande à activer, avec ses caractéristiques figées")
public record DemandeActivationRequest(
        @NotNull UUID commandeId,
        @NotNull UUID elementId,
        @Schema(example = "DRONE-600") @NotBlank String droneId,
        @Schema(example = "C2") @NotBlank String typeService,
        @Schema(description = "Acceptation de la commande par commande-service", example = "2026-10-31T14:02:11.204Z")
        @NotNull Instant acceptedAt,
        @NotNull @Valid Caracteristiques caracteristiques) {

    @Schema(name = "CaracteristiquesDemandees", description = "Caractéristiques copiées du catalogue par commande-service")
    public record Caracteristiques(
            @Schema(example = "2") int sst,
            @Schema(example = "000001") @NotBlank String sd,
            @Schema(example = "c2") @NotBlank String dnn,
            @Schema(example = "7") int fiveQi,
            @Schema(example = "2") int arp,
            @Schema(example = "\"20 Mbps\"") @NotBlank String ambrMontant,
            @Schema(example = "\"20 Mbps\"") @NotBlank String ambrDescendant) {}
}
