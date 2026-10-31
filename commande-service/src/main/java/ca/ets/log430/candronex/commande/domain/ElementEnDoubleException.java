package ca.ets.log430.candronex.commande.domain;

import ca.ets.log430.candronex.commun.domaine.RegleMetierException;

/** Deux éléments d'une même commande demandent le même service pour le même drone → 422. */
public class ElementEnDoubleException extends RegleMetierException {
    public ElementEnDoubleException() {
        super("ELEMENT_EN_DOUBLE", "Deux éléments demandent le même service pour le même drone");
    }
}
