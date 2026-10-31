package ca.ets.log430.candronex.slice.adapter.in.web;

import ca.ets.log430.candronex.slice.domain.Service;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/** DTO d'un service de l'inventaire : jamais d'IMSI. */
@Schema(description = "Service réseau d'un drone, dans l'inventaire")
public record ServiceResponse(
        @Schema(example = "4d8e2b1f-7a3c-4e95-8b06-1c9f5a2d7e40") String serviceId,
        @Schema(example = "6f1c2a9e-0b7d-4c3e-9a51-8d2e4f6b1a07") String commandeId,
        @Schema(example = "0c9a7d3b-6e2f-4a81-b5d0-3f7e1c8a2b94") String elementId,
        @Schema(example = "DRONE-600") String droneId,
        @Schema(example = "C2") String typeService,
        @Schema(description = "Identifiant de la slice (sst-sd)", example = "2-000001") String snssai,
        @Schema(description = "ECHEC_INDETERMINE : délai dépassé, issue inconnue (vérifier avant toute reprise)",
                allowableValues = {"EN_ATTENTE", "EN_ACTIVATION", "ACTIF", "ECHEC", "ECHEC_INDETERMINE"},
                example = "ACTIF") String etat,
        @Schema(description = "Acceptation de la commande : début de la mesure du délai d'activation",
                example = "2026-10-31T14:02:11.204Z") Instant acceptedAt,
        @Schema(description = "Absent tant que le service n'est pas ACTIF", example = "2026-10-31T14:02:12.588Z")
        Instant activatedAt,
        @Schema(description = "Absent sauf en cas d'échec") String causeEchec) {

    public static ServiceResponse depuis(Service s) {
        return new ServiceResponse(
                s.id().valeur().toString(),
                s.origine().commandeId().toString(),
                s.origine().elementId().toString(),
                s.droneId().valeur(),
                s.typeService(),
                s.configuration().snssai(),
                s.etat().name(),
                s.acceptedAt(),
                s.activatedAt().orElse(null),
                s.causeEchec().orElse(null));
    }
}
