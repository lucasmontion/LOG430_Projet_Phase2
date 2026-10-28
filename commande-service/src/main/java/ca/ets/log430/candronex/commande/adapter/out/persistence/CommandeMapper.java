package ca.ets.log430.candronex.commande.adapter.out.persistence;

import ca.ets.log430.candronex.commande.domain.*;
import ca.ets.log430.candronex.commun.domaine.ClientId;
import org.springframework.stereotype.Component;

import java.util.List;

/** Traduit entre l'agrégat Commande (domaine) et ses entités JPA (persistance). */
@Component
public class CommandeMapper {

    public CommandeJpaEntity versEntite(Commande commande) {
        CommandeJpaEntity entite = new CommandeJpaEntity(
                commande.id().valeur(), commande.clientId().valeur(), commande.cleIdempotence().valeur());
        List<ElementCommande> elements = commande.elements();
        for (int rang = 0; rang < elements.size(); rang++) {
            ElementCommande e = elements.get(rang);
            CaracteristiquesFigees c = e.caracteristiques();
            entite.ajouterElement(new ElementCommandeJpaEntity(
                    e.id().valeur(), rang, e.droneId().valeur(), e.typeService().valeur(), e.etat().name(),
                    c.sst(), c.sd(), c.dnn(), c.fiveQi(), c.arp(), c.ambrMontant(), c.ambrDescendant()));
        }
        return entite;
    }

    public Commande versDomaine(CommandeJpaEntity entite) {
        List<ElementCommande> elements = entite.getElements().stream()
                .map(e -> ElementCommande.reconstituer(
                        new ElementId(e.getId()),
                        new DroneId(e.getDroneId()),
                        new TypeService(e.getTypeService()),
                        new CaracteristiquesFigees(e.getSst(), e.getSd(), e.getDnn(), e.getFiveQi(), e.getArp(),
                                e.getAmbrMontant(), e.getAmbrDescendant()),
                        EtatElement.valueOf(e.getEtat())))
                .toList();
        return Commande.reconstituer(
                new CommandeId(entite.getId()),
                new ClientId(entite.getClientId()),
                new CleIdempotence(entite.getCleIdempotence()),
                elements);
    }
}
