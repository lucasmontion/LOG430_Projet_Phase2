package ca.ets.log430.candronex.drone.domain;

import ca.ets.log430.candronex.commun.domaine.DonneeInvalideException;

import java.util.regex.Pattern;

/** Numéro de série de la carte SIM ou du profil eSIM (19 ou 20 chiffres). Masqué dans les journaux. */
public record Iccid(String valeur) {
    private static final Pattern FORMAT = Pattern.compile("\\d{19,20}");

    public Iccid {
        if (valeur == null || !FORMAT.matcher(valeur).matches()) {
            throw new DonneeInvalideException("ICCID_INVALIDE", "L'ICCID doit contenir 19 ou 20 chiffres");
        }
    }

    @Override
    public String toString() {
        return "****" + valeur.substring(valeur.length() - 4);
    }
}
