package ca.ets.log430.candronex.commande.domain;

import ca.ets.log430.candronex.commun.domaine.DonneeInvalideException;

import java.util.UUID;

public record CommandeId(UUID valeur) {
    public static CommandeId nouveau() {
        return new CommandeId(UUID.randomUUID());
    }

    public static CommandeId depuis(String texte) {
        try {
            return new CommandeId(UUID.fromString(texte));
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new DonneeInvalideException("COMMANDE_ID_INVALIDE", "Identifiant de commande invalide");
        }
    }
}
