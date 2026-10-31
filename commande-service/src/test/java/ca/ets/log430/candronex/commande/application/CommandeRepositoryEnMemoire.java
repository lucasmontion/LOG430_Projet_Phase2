package ca.ets.log430.candronex.commande.application;

import ca.ets.log430.candronex.commande.domain.*;
import ca.ets.log430.candronex.commun.domaine.ClientId;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Adaptateur de test branché sur le port CommandeRepository. */
class CommandeRepositoryEnMemoire implements CommandeRepository {
    final List<Commande> commandes = new ArrayList<>();

    /** Simule une requête simultanée : la prochaine lecture par clé ne voit pas encore la commande déjà enregistrée. */
    boolean lectureEnRetard = false;

    @Override
    public Commande enregistrerSiAbsent(Commande commande) {
        Optional<Commande> existante = trouverParCle(commande.clientId(), commande.cleIdempotence());
        if (existante.isPresent()) {
            return existante.get();
        }
        commandes.add(commande);
        return commande;
    }

    @Override
    public Optional<Commande> trouver(ClientId clientId, CommandeId commandeId) {
        return commandes.stream()
                .filter(c -> c.appartientA(clientId) && c.id().equals(commandeId))
                .findFirst();
    }

    @Override
    public Optional<Commande> trouverParCle(ClientId clientId, CleIdempotence cle) {
        if (lectureEnRetard) {
            lectureEnRetard = false;
            return Optional.empty();
        }
        return commandes.stream()
                .filter(c -> c.appartientA(clientId) && c.cleIdempotence().equals(cle))
                .findFirst();
    }
}
