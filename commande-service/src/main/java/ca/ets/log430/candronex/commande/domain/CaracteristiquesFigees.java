package ca.ets.log430.candronex.commande.domain;

import java.util.Objects;

/**
 * Paramètres réseau COPIÉS du catalogue au moment de la soumission.
 * Ils ne changent plus jamais : la commande est un fait contractuel,
 * une évolution du catalogue ne la réécrit pas.
 */
public record CaracteristiquesFigees(
        int sst,
        String sd,
        String dnn,
        int fiveQi,
        int arp,
        String ambrMontant,
        String ambrDescendant) {

    public CaracteristiquesFigees {
        Objects.requireNonNull(sd, "sd");
        Objects.requireNonNull(dnn, "dnn");
        Objects.requireNonNull(ambrMontant, "ambrMontant");
        Objects.requireNonNull(ambrDescendant, "ambrDescendant");
    }
}
