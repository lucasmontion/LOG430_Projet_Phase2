package ca.ets.log430.candronex.commande.adapter.in.web;

import ca.ets.log430.candronex.commande.domain.CaracteristiquesFigees;
import ca.ets.log430.candronex.commande.domain.Commande;
import ca.ets.log430.candronex.commande.domain.ElementCommande;

import java.util.List;

/** DTO web retourné au client : l'état global ET l'état de chaque élément. */
public record CommandeResponse(String commandeId, String etat, List<ElementResponse> elements) {

    public static CommandeResponse depuis(Commande commande) {
        return new CommandeResponse(
                commande.id().valeur().toString(),
                commande.etat().name(),
                commande.elements().stream().map(ElementResponse::depuis).toList());
    }

    public record ElementResponse(String elementId, String droneId, String typeService,
                                  String etat, CaracteristiquesResponse caracteristiques) {
        static ElementResponse depuis(ElementCommande e) {
            CaracteristiquesFigees c = e.caracteristiques();
            return new ElementResponse(
                    e.id().valeur().toString(), e.droneId().valeur(), e.typeService().valeur(), e.etat().name(),
                    new CaracteristiquesResponse(c.sst(), c.sd(), c.dnn(), c.fiveQi(), c.arp(),
                            c.ambrMontant(), c.ambrDescendant()));
        }
    }

    public record CaracteristiquesResponse(int sst, String sd, String dnn, int fiveQi, int arp,
                                           String ambrMontant, String ambrDescendant) {}
}
