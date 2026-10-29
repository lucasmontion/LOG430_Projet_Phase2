package ca.ets.log430.candronex.slice.domain;

import java.util.Objects;

/**
 * Réponse du réseau à une demande de configuration, dans le vocabulaire du domaine.
 * Chaque issue correspond à une transition du §6.2 : SUCCES -> ACTIF, ECHEC -> ECHEC,
 * INDETERMINE -> ECHEC_INDETERMINE.
 */
public record ResultatProvisionnement(Issue issue, String cause) {

    public enum Issue { SUCCES, ECHEC, INDETERMINE }

    public ResultatProvisionnement {
        Objects.requireNonNull(issue, "issue");
    }

    public static ResultatProvisionnement succes() {
        return new ResultatProvisionnement(Issue.SUCCES, null);
    }

    /** Échec certain : la configuration n'a pas été appliquée (refus du réseau, réseau injoignable). */
    public static ResultatProvisionnement echec(String cause) {
        return new ResultatProvisionnement(Issue.ECHEC, Objects.requireNonNull(cause, "cause"));
    }

    /** Issue inconnue : la configuration a peut-être été appliquée (délai dépassé, connexion interrompue). */
    public static ResultatProvisionnement indetermine(String cause) {
        return new ResultatProvisionnement(Issue.INDETERMINE, Objects.requireNonNull(cause, "cause"));
    }
}
