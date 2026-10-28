package ca.ets.log430.candronex.commande.domain;

import ca.ets.log430.candronex.commun.domaine.RessourceIntrouvableException;

/** Traduction, dans le vocabulaire de commande, de l'absence du drone (inexistant ou autre client). */
public class DroneNonTrouveException extends RessourceIntrouvableException {
    public DroneNonTrouveException(DroneId id) {
        super("DRONE_INTROUVABLE", "Drone " + id.valeur() + " introuvable");
    }
}
