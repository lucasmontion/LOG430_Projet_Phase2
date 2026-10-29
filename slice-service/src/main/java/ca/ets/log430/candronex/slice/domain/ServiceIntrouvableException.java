package ca.ets.log430.candronex.slice.domain;

import ca.ets.log430.candronex.commun.domaine.RessourceIntrouvableException;

/** Même erreur pour un service inexistant et pour celui d'un autre client (non-divulgation). */
public class ServiceIntrouvableException extends RessourceIntrouvableException {
    public ServiceIntrouvableException(ServiceId id) {
        super("SERVICE_INTROUVABLE", "Service " + id.valeur() + " introuvable");
    }
}
