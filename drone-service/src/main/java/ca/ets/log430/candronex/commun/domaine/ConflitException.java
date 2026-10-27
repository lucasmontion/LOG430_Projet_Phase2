package ca.ets.log430.candronex.commun.domaine;

/** Conflit avec une ressource existante (doublon) → 409 */
public abstract class ConflitException extends ExceptionMetier {
    protected ConflitException(String code, String message) {
        super(code, message);
    }
}
