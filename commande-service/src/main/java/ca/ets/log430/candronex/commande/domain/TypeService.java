package ca.ets.log430.candronex.commande.domain;

import ca.ets.log430.candronex.commun.domaine.DonneeInvalideException;

/** Type de service commandé (C2, IMAGERIE). Son existence est vérifiée auprès du catalogue. */
public record TypeService(String valeur) {
    public TypeService {
        if (valeur == null || valeur.isBlank()) {
            throw new DonneeInvalideException("TYPE_SERVICE_INVALIDE", "Le typeService est obligatoire");
        }
    }
}
