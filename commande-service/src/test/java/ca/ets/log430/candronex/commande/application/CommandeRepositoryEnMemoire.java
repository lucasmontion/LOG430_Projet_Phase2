package ca.ets.log430.candronex.commande.application;

import ca.ets.log430.candronex.commande.domain.*;
import ca.ets.log430.candronex.commun.domaine.ClientId;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Adaptateur de test branché sur le port CommandeRepository. */
class CommandeRepositoryEnMemoire implements CommandeRepository {
    final List<Commande> commandes = new ArrayList<>();

    @Override
    public void sauvegarder(Commande commande) { commandes.add(commande); }

    @Override
    public Optional<Commande> trouver(ClientId clientId, CommandeId commandeId) {
        return commandes.stream()
                .filter(c -> c.appartientA(clientId) && c.id().equals(commandeId))
                .findFirst();
    }

    @Override
    public Optional<Commande> trouverParCle(ClientId clientId, CleIdempotence cle) {
        return commandes.stream()
                .filter(c -> c.appartientA(clientId) && c.cleIdempotence().equals(cle))
                .findFirst();
    }
}
