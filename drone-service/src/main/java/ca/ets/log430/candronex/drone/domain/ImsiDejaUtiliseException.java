package ca.ets.log430.candronex.drone.domain;

import ca.ets.log430.candronex.commun.domaine.ConflitException;

/** Le message ne révèle ni l'IMSI ni le client qui le détient. */
public class ImsiDejaUtiliseException extends ConflitException {
    public ImsiDejaUtiliseException() {
        super("IMSI_DEJA_UTILISE", "Cet IMSI est déjà associé à un drone");
    }
}
