package ca.ets.log430.candronex.commun.domaine;

/** Donnée mal formée (format, champ manquant) → 400 */
public class DonneeInvalideException extends ExceptionMetier {
    public DonneeInvalideException(String code, String message) {
        super(code, message);
    }
}
