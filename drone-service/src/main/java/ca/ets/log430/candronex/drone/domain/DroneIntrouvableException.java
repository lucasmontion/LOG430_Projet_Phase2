package ca.ets.log430.candronex.drone.domain;

import ca.ets.log430.candronex.commun.domaine.RessourceIntrouvableException;

/** Même erreur pour un drone inexistant et pour le drone d'un autre client (non-divulgation). */
public class DroneIntrouvableException extends RessourceIntrouvableException {
    public DroneIntrouvableException(DroneId id) {
        super("DRONE_INTROUVABLE", "Drone " + id.valeur() + " introuvable");
    }
}
