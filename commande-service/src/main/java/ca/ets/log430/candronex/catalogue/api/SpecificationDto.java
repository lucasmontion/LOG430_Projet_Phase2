package ca.ets.log430.candronex.catalogue.api;

/**
 * Objet d'échange exposé aux autres modules : types simples uniquement,
 * pour qu'aucun module ne dépende des classes internes du catalogue.
 */
public record SpecificationDto(
        String typeService,
        String nom,
        int sst,
        String sd,
        String dnn,
        int fiveQi,
        int arp,
        String ambrMontant,
        String ambrDescendant) {}
