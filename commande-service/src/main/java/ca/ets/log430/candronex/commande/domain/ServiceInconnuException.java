package ca.ets.log430.candronex.commande.domain;

import ca.ets.log430.candronex.commun.domaine.RegleMetierException;

public class ServiceInconnuException extends RegleMetierException {
    public ServiceInconnuException(TypeService type) {
        super("SERVICE_INCONNU", "Le service " + type.valeur() + " n'existe pas au catalogue");
    }
}
