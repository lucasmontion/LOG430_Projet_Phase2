package ca.ets.log430.candronex.slice.domain;

import ca.ets.log430.candronex.commun.domaine.DonneeInvalideException;

import java.util.UUID;

/** Identifiant d'un service dans l'inventaire. */
public record ServiceId(UUID valeur) {
    public static ServiceId nouveau() {
        return new ServiceId(UUID.randomUUID());
    }

    public static ServiceId depuis(String texte) {
        try {
            return new ServiceId(UUID.fromString(texte));
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new DonneeInvalideException("SERVICE_ID_INVALIDE", "Identifiant de service invalide");
        }
    }
}
