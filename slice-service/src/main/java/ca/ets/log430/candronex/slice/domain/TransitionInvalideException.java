package ca.ets.log430.candronex.slice.domain;

import ca.ets.log430.candronex.commun.domaine.RegleMetierException;

/** Transition absente du diagramme d'état (§6.2) : l'agrégat la refuse. */
public class TransitionInvalideException extends RegleMetierException {
    public TransitionInvalideException(EtatService depuis, String operation) {
        super("TRANSITION_INVALIDE", "Transition « " + operation + " » interdite depuis l'état " + depuis);
    }
}
