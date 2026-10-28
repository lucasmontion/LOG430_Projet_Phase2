package ca.ets.log430.candronex.commande.domain;

import java.util.UUID;

public record ElementId(UUID valeur) {
    public static ElementId nouveau() {
        return new ElementId(UUID.randomUUID());
    }
}
