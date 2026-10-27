package ca.ets.log430.candronex.drone.domain;

import ca.ets.log430.candronex.commun.domaine.ConflitException;

/** Le message ne révèle ni l'ICCID ni le client qui le détient. */
public class IccidDejaUtiliseException extends ConflitException {
    public IccidDejaUtiliseException() {
        super("ICCID_DEJA_UTILISE", "Cet ICCID est déjà associé à un drone");
    }
}