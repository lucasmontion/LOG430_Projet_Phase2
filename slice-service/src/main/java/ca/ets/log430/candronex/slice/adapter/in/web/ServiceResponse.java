package ca.ets.log430.candronex.slice.adapter.in.web;

import ca.ets.log430.candronex.slice.domain.Service;

import java.time.Instant;

/** DTO d'un service de l'inventaire : jamais d'IMSI. */
public record ServiceResponse(
        String serviceId,
        String commandeId,
        String elementId,
        String droneId,
        String typeService,
        String snssai,
        String etat,
        Instant acceptedAt,
        Instant activatedAt,
        String causeEchec) {

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
