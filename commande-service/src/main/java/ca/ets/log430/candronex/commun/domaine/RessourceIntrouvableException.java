package ca.ets.log430.candronex.commun.domaine;

/** Ressource inexistante ou appartenant à un autre client → 404 */
public abstract class RessourceIntrouvableException extends ExceptionMetier {
    protected RessourceIntrouvableException(String code, String message) {
        super(code, message);
    }
}
