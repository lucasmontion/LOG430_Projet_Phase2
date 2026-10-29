package ca.ets.log430.candronex.slice.domain;

/**
 * Fait partie du contrat de IdentiteReseauPort : l'identité réseau n'a pas pu être obtenue
 * (drone-service injoignable, délai dépassé). Aucune configuration n'a été tentée : échec certain.
 */
public class IdentiteReseauIndisponibleException extends RuntimeException {
    public IdentiteReseauIndisponibleException(String cause) {
        super(cause);
    }
}
