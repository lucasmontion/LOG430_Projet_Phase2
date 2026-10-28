package ca.ets.log430.candronex.commun.domaine;

/**
 * Racine des erreurs du domaine. Chaque erreur porte un code stable,
 * que l'adaptateur web traduit en code HTTP (voir ADR-0003).
 */
public abstract class ExceptionMetier extends RuntimeException {
    private final String code;

    protected ExceptionMetier(String code, String message) {
        super(message);
        this.code = code;
    }

    public String code() { return code; }
}
