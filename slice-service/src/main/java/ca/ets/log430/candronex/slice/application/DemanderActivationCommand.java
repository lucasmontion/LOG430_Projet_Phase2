package ca.ets.log430.candronex.slice.application;

import java.time.Instant;
import java.util.UUID;

/** Données d'entrée de UC-06 (demande d'activation d'un élément de commande), indépendantes de HTTP. */
public record DemanderActivationCommand(
        String clientId,
        UUID commandeId,
        UUID elementId,
        String droneId,
        String typeService,
        Instant acceptedAt,
        int sst, String sd, String dnn, int fiveQi, int arp, String ambrMontant, String ambrDescendant) {}
