package ca.ets.log430.candronex.commande.domain;

import ca.ets.log430.candronex.commun.domaine.RegleMetierException;

public class DroneNonEligibleException extends RegleMetierException {
    public DroneNonEligibleException(DroneId id, String statut) {
        super("DRONE_NON_ELIGIBLE", "Le drone " + id.valeur() + " n'est pas actif (statut " + statut + ")");
    }
}
