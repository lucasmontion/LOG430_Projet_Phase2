package ca.ets.log430.candronex.commande.domain;

import ca.ets.log430.candronex.commun.domaine.DonneeInvalideException;

/** Clé fournie par le client : deux requêtes avec la même clé sont la même demande. */
public record CleIdempotence(String valeur) {
    public CleIdempotence {
        if (valeur == null || valeur.isBlank() || valeur.length() > 100) {
            throw new DonneeInvalideException("CLE_IDEMPOTENCE_INVALIDE",
                    "L'en-tête Idempotency-Key est obligatoire (100 caractères max.)");
        }
    }
}
