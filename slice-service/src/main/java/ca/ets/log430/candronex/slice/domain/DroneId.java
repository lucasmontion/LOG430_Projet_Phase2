package ca.ets.log430.candronex.slice.domain;

import ca.ets.log430.candronex.commun.domaine.DonneeInvalideException;

/** Référence au drone, dans le vocabulaire du contexte slice (le drone appartient à drone-service). */
public record DroneId(String valeur) {
    public DroneId {
        if (valeur == null || valeur.isBlank()) {
            throw new DonneeInvalideException("DRONE_ID_INVALIDE", "Le droneId est obligatoire");
        }
    }
}
