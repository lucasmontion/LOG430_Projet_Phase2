package ca.ets.log430.candronex.drone.domain;

import ca.ets.log430.candronex.commun.domaine.DonneeInvalideException;

import java.util.Arrays;

/** Forme du support d'identité réseau : carte physique ou profil embarqué (ensemble fermé, fixé par la norme). */
public enum TypeSim {
    SIM, ESIM;

    /** Traduit la valeur reçue de l'API ; refuse toute valeur inconnue avec une erreur métier (400). */
    public static TypeSim depuis(String valeur) {
        return Arrays.stream(values())
                .filter(t -> t.name().equals(valeur))
                .findFirst()
                .orElseThrow(() -> new DonneeInvalideException("TYPE_SIM_INVALIDE",
                        "Le typeSim doit être SIM ou ESIM"));
    }
}