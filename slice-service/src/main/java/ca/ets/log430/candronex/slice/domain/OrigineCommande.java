package ca.ets.log430.candronex.slice.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * L'élément de commande qui a demandé ce service. C'est la clé d'idempotence de l'activation :
 * un seul service par (commande, élément), donc jamais deux associations slice <-> drone
 * pour une même demande logique.
 */
public record OrigineCommande(UUID commandeId, UUID elementId) {
    public OrigineCommande {
        Objects.requireNonNull(commandeId, "commandeId");
        Objects.requireNonNull(elementId, "elementId");
    }
}
