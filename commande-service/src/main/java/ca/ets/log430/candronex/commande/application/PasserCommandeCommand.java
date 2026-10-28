package ca.ets.log430.candronex.commande.application;

import java.util.List;

/** Données d'entrée de UC-04, indépendantes de HTTP. */
public record PasserCommandeCommand(String clientId, String cleIdempotence, List<Element> elements) {
    public record Element(String droneId, String typeService) {}
}
