package ca.ets.log430.candronex.commande.adapter.in.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/** DTO web reçu par POST /api/v1/commandes. Le client ne fournit JAMAIS les caractéristiques réseau. */
public record PasserCommandeRequest(@NotNull List<@Valid Element> elements) {
    public record Element(@NotBlank String droneId, @NotBlank String typeService) {}
}
