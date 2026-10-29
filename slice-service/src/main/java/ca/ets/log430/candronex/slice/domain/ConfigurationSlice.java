package ca.ets.log430.candronex.slice.domain;

import ca.ets.log430.candronex.commun.domaine.DonneeInvalideException;

/**
 * Paramètres réseau à appliquer, déterminés à partir des caractéristiques de l'élément de commande
 * (UC-06, étape 2) : S-NSSAI (sst + sd), DNN et QoS (5QI, ARP, AMBR).
 */
public record ConfigurationSlice(
        int sst,
        String sd,
        String dnn,
        int fiveQi,
        int arp,
        String ambrMontant,
        String ambrDescendant) {

    public ConfigurationSlice {
        exiger(sst >= 0 && sst <= 255, "SST_INVALIDE", "Le sst doit être entre 0 et 255");
        exiger(sd != null && sd.matches("[0-9A-Fa-f]{6}"), "SD_INVALIDE", "Le sd doit contenir 6 chiffres hexadécimaux");
        exiger(dnn != null && !dnn.isBlank(), "DNN_INVALIDE", "Le dnn est obligatoire");
        exiger(fiveQi > 0, "FIVEQI_INVALIDE", "Le fiveQi doit être positif");
        exiger(arp >= 1 && arp <= 15, "ARP_INVALIDE", "L'arp doit être entre 1 et 15");
        exiger(ambrMontant != null && !ambrMontant.isBlank(), "AMBR_INVALIDE", "Le débit montant est obligatoire");
        exiger(ambrDescendant != null && !ambrDescendant.isBlank(), "AMBR_INVALIDE", "Le débit descendant est obligatoire");
    }

    /** S-NSSAI au format usuel « sst-sd » (ex. 2-000001), identifiant de la slice dans le réseau. */
    public String snssai() {
        return sst + "-" + sd;
    }

    private static void exiger(boolean condition, String code, String message) {
        if (!condition) throw new DonneeInvalideException(code, message);
    }
}
