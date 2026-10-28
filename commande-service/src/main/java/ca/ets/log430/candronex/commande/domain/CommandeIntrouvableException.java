package ca.ets.log430.candronex.commande.domain;

import ca.ets.log430.candronex.commun.domaine.RessourceIntrouvableException;

/** Même erreur pour une commande inexistante et pour celle d'un autre client. */
public class CommandeIntrouvableException extends RessourceIntrouvableException {
    public CommandeIntrouvableException(CommandeId id) {
        super("COMMANDE_INTROUVABLE", "Commande " + id.valeur() + " introuvable");
    }
}
