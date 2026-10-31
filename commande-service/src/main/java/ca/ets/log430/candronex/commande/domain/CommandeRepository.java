package ca.ets.log430.candronex.commande.domain;

import ca.ets.log430.candronex.commun.domaine.ClientId;

import java.util.Optional;

/** Port de sortie : persistance des commandes. Toute lecture est filtrée par client. */
public interface CommandeRepository {
    /**
     * Enregistre la commande, sauf si une commande du même client avec la même clé existe déjà
     * (requête simultanée) : renvoie alors cette commande existante, sans rien enregistrer.
     */
    Commande enregistrerSiAbsent(Commande commande);
    Optional<Commande> trouver(ClientId clientId, CommandeId commandeId);
    Optional<Commande> trouverParCle(ClientId clientId, CleIdempotence cle);
}
