package ca.ets.log430.candronex.commande.domain;

import ca.ets.log430.candronex.commun.domaine.ConflitException;

/** La clé d'idempotence a déjà servi pour une commande au contenu différent → 409. */
public class CleIdempotenceReutiliseeException extends ConflitException {
    public CleIdempotenceReutiliseeException() {
        super("CLE_IDEMPOTENCE_REUTILISEE",
                "Cette clé d'idempotence a déjà servi pour une commande au contenu différent");
    }
}
