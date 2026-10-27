package ca.ets.log430.candronex.drone.domain;

import ca.ets.log430.candronex.commun.domaine.DonneeInvalideException;

import java.util.regex.Pattern;

/** Identifiant métier du drone, choisi par le client (ex. DRONE-001). */
public record DroneId(String valeur) {
    private static final Pattern FORMAT = Pattern.compile("[A-Za-z0-9-]{1,64}");

    public DroneId {
        if (valeur == null || !FORMAT.matcher(valeur).matches()) {
            throw new DonneeInvalideException("DRONE_ID_INVALIDE",
                    "Le droneId est obligatoire (lettres, chiffres et tirets, 64 caractères max.)");
        }
    }
}
