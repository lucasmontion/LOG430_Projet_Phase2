package ca.ets.log430.candronex.commun.domaine;

/** Requête valide mais refusée par une règle métier → 422 */
public abstract class RegleMetierException extends ExceptionMetier {
    protected RegleMetierException(String code, String message) {
        super(code, message);
    }
}
