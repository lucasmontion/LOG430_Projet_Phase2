package ca.ets.log430.candronex.drone.domain;

import ca.ets.log430.candronex.commun.domaine.DonneeInvalideException;

import java.util.regex.Pattern;

/**
 * Identité réseau de l'abonné (15 chiffres). Donnée sensible (Loi 25) :
 * confinée au module drone, et toString() est masqué pour ne jamais fuiter dans les journaux.
 */
public record Imsi(String valeur) {
    private static final Pattern FORMAT = Pattern.compile("\\d{15}");

    public Imsi {
        if (valeur == null || !FORMAT.matcher(valeur).matches()) {
            throw new DonneeInvalideException("IMSI_INVALIDE", "L'IMSI doit contenir exactement 15 chiffres");
        }
    }

    /** Ex. 999700000010231 -> 99970*******231 */
    public String masque() {
        return valeur.substring(0, 5) + "*******" + valeur.substring(12);
    }

    @Override
    public String toString() {
        return masque();
    }
}
