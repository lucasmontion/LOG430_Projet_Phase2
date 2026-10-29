package ca.ets.log430.candronex.slice.application;

import ca.ets.log430.candronex.slice.domain.Service;

/** nouvelle = false quand la demande est un rejeu (même commande, même élément). */
public record ResultatDemande(Service service, boolean nouvelle) {}
