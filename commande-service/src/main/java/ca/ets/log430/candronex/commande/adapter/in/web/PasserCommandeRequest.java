package ca.ets.log430.candronex.commande.adapter.in.web;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/** DTO web reçu par POST /api/commande-service/v1/commande. Le client ne fournit JAMAIS les caractéristiques réseau. */
@Schema(description = "Commande : au moins un élément, sans doublon (drone, service)")
public record PasserCommandeRequest(
        @ArraySchema(minItems = 1)
        @NotNull List<@Valid Element> elements) {

    @Schema(name = "ElementDemande", description = "Un service demandé pour un drone du client")
    public record Element(
            @Schema(description = "Drone enregistré par ce client, à l'état ACTIF", example = "DRONE-600")
            @NotBlank String droneId,
            @Schema(description = "Service du catalogue", allowableValues = {"C2", "IMAGERIE"}, example = "C2")
            @NotBlank String typeService) {}
}
