package ca.ets.log430.candronex.commande.domain;

/**
 * Cycle de vie d'un élément (§6.2). Seul RECU est atteint en Phase 1 ;
 * les autres états sont modélisés pour UC-06, UC-09, UC-10 et UC-15.
 */
public enum EtatElement {
    RECU, EN_COURS, ACTIF, ECHEC, ANNULE, COMPENSE;

    boolean estEnTraitement() {
        return this == RECU || this == EN_COURS;
    }

    /** Terminé sans échec, mais n'est plus actif (annulé avant activation, ou retiré après). */
    boolean estRetire() {
        return this == ANNULE || this == COMPENSE;
    }
}
