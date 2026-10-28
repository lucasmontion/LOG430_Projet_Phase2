package ca.ets.log430.candronex.catalogue.domain;

import ca.ets.log430.candronex.commun.domaine.DonneeInvalideException;

import java.util.regex.Pattern;

/**
 * Paramètres réseau d'un service (dossier de domaine §7.3).
 * Objet valeur : ces valeurs seront copiées telles quelles dans chaque élément de commande.
 */
public record Caracteristiques(
        int sst,
        String sd,
        String dnn,
        int fiveQi,
        int arp,
        String ambrMontant,
        String ambrDescendant) {

    private static final Pattern SD = Pattern.compile("[0-9A-Fa-f]{6}");
    private static final Pattern DEBIT = Pattern.compile("\\d+ (bps|Kbps|Mbps|Gbps)");

    public Caracteristiques {
        exiger(sst >= 0 && sst <= 255, "SST_INVALIDE", "Le sst doit être entre 0 et 255");
        exiger(sd != null && SD.matcher(sd).matches(), "SD_INVALIDE", "Le sd doit contenir 6 chiffres hexadécimaux");
        exiger(dnn != null && !dnn.isBlank(), "DNN_INVALIDE", "Le dnn est obligatoire");
        exiger(fiveQi > 0, "FIVEQI_INVALIDE", "Le fiveQi doit être positif");
        exiger(arp >= 1 && arp <= 15, "ARP_INVALIDE", "L'arp doit être entre 1 et 15");
        exiger(ambrMontant != null && DEBIT.matcher(ambrMontant).matches(), "AMBR_INVALIDE", "Débit montant invalide (ex. 20 Mbps)");
        exiger(ambrDescendant != null && DEBIT.matcher(ambrDescendant).matches(), "AMBR_INVALIDE", "Débit descendant invalide (ex. 20 Mbps)");
    }

    private static void exiger(boolean condition, String code, String message) {
        if (!condition) throw new DonneeInvalideException(code, message);
    }
}
