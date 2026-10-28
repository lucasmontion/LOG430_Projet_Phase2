package ca.ets.log430.candronex.commande.domain;

/** État global d'une commande : toujours DÉRIVÉ de l'état de ses éléments, jamais saisi. */
public enum EtatCommande {
    RECUE, EN_COURS, COMPLETEE, PARTIELLE, ECHOUEE, ANNULEE
}
