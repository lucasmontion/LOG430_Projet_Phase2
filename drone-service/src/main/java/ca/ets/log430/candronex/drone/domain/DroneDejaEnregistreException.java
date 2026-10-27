package ca.ets.log430.candronex.drone.domain;

import ca.ets.log430.candronex.commun.domaine.ConflitException;

public class DroneDejaEnregistreException extends ConflitException {
    public DroneDejaEnregistreException(DroneId id) {
        super("DRONE_DEJA_ENREGISTRE", "Le drone " + id.valeur() + " est déjà enregistré pour ce client");
    }
}
