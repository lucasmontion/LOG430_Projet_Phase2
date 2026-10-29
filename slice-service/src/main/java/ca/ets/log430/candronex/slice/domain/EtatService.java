package ca.ets.log430.candronex.slice.domain;

/**
 * Cycle de vie d'un service dans l'inventaire (rapport §6.2).
 * EN_ATTENTE est l'état initial ; ACTIF est l'état final en Phase 2.
 */
public enum EtatService {
    EN_ATTENTE,
    EN_ACTIVATION,
    ACTIF,
    /** Échec certain : aucune configuration n'a été appliquée au réseau. */
    ECHEC,
    /** Échec explicite dont l'issue est inconnue (délai dépassé) : vérifier l'état réel avant toute reprise. */
    ECHEC_INDETERMINE
}
