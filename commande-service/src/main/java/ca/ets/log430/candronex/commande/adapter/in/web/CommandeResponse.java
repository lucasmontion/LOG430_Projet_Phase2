package ca.ets.log430.candronex.commande.adapter.in.web;

import ca.ets.log430.candronex.commande.domain.CaracteristiquesFigees;
import ca.ets.log430.candronex.commande.domain.Commande;
import ca.ets.log430.candronex.commande.domain.ElementCommande;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/** DTO web retourné au client : l'état global ET l'état de chaque élément. */
@Schema(description = "Commande du client")
public record CommandeResponse(
        @Schema(example = "6f1c2a9e-0b7d-4c3e-9a51-8d2e4f6b1a07") String commandeId,
        @Schema(description = "Dérivé de l'état des éléments",
                allowableValues = {"RECUE", "EN_COURS", "COMPLETEE", "PARTIELLE", "ECHOUEE", "ANNULEE"},
                example = "EN_COURS") String etat,
        List<ElementResponse> elements) {

    public static CommandeResponse depuis(Commande commande) {
        return new CommandeResponse(
                commande.id().valeur().toString(),
                commande.etat().name(),
                commande.elements().stream().map(ElementResponse::depuis).toList());
    }

    @Schema(description = "Un élément de la commande : un service pour un drone")
    public record ElementResponse(
            @Schema(example = "0c9a7d3b-6e2f-4a81-b5d0-3f7e1c8a2b94") String elementId,
            @Schema(example = "DRONE-600") String droneId,
            @Schema(example = "C2") String typeService,
            @Schema(allowableValues = {"RECU", "EN_COURS", "ACTIF", "ECHEC", "ANNULE", "COMPENSE"},
                    example = "EN_COURS") String etat,
            CaracteristiquesResponse caracteristiques) {

        static ElementResponse depuis(ElementCommande e) {
            CaracteristiquesFigees c = e.caracteristiques();
            return new ElementResponse(
                    e.id().valeur().toString(), e.droneId().valeur(), e.typeService().valeur(), e.etat().name(),
                    new CaracteristiquesResponse(c.sst(), c.sd(), c.dnn(), c.fiveQi(), c.arp(),
                            c.ambrMontant(), c.ambrDescendant()));
        }
    }

    @Schema(description = "Caractéristiques réseau copiées du catalogue au moment de la commande (figées)")
    public record CaracteristiquesResponse(
            @Schema(description = "Slice/Service Type", example = "2") int sst,
            @Schema(description = "Slice Differentiator", example = "000001") String sd,
            @Schema(description = "Data Network Name", example = "c2") String dnn,
            @Schema(description = "Classe de qualité de service 5G", example = "7") int fiveQi,
            @Schema(description = "Priorité d'allocation et de rétention", example = "2") int arp,
            @Schema(description = "Débit agrégé maximal montant", example = "\"20 Mbps\"") String ambrMontant,
            @Schema(description = "Débit agrégé maximal descendant", example = "\"20 Mbps\"") String ambrDescendant) {}
}
