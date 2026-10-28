package ca.ets.log430.candronex.commande.domain;

import ca.ets.log430.candronex.commun.domaine.DonneeInvalideException;

/**
 * Référence au drone visé, dans le vocabulaire du module commande.
 * Distinct du DroneId du module drone : chaque contexte a son propre modèle.
 */
public record DroneId(String valeur) {
    public DroneId {
        if (valeur == null || valeur.isBlank()) {
            throw new DonneeInvalideException("DRONE_ID_INVALIDE", "Le droneId est obligatoire");
        }
    }
}
