package ca.ets.log430.candronex.slice.adapter.in.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

/** DTO reçu par POST /api/slice-service/v1/activation (appelé par commande-service seulement). */
public record DemandeActivationRequest(
        @NotNull UUID commandeId,
        @NotNull UUID elementId,
        @NotBlank String droneId,
        @NotBlank String typeService,
        @NotNull Instant acceptedAt,
        @NotNull @Valid Caracteristiques caracteristiques) {

    public record Caracteristiques(int sst, @NotBlank String sd, @NotBlank String dnn, int fiveQi, int arp,
                                   @NotBlank String ambrMontant, @NotBlank String ambrDescendant) {}
}
