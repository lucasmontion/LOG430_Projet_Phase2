package ca.ets.log430.candronex.commande.application;

import ca.ets.log430.candronex.commande.domain.Commande;

/** nouvelle = false quand la requête est un rejeu (même clé d'idempotence). */
public record ResultatCommande(Commande commande, boolean nouvelle) {}
