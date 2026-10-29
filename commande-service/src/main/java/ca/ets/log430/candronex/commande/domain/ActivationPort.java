package ca.ets.log430.candronex.commande.domain;

import ca.ets.log430.candronex.commun.domaine.ClientId;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

/**
 * Port de sortie : ce dont la commande a besoin du contexte Activation (UC-06), dans son vocabulaire.
 * La commande DEMANDE l'activation de ses éléments, puis LIT leur état ; l'activation ne rappelle jamais
 * la commande (pas de cycle de dépendances). Les deux opérations sont idempotentes côté activation.
 */
public interface ActivationPort {

    /**
     * Demande l'activation d'un élément de commande.
     * @return l'état de l'élément qui en résulte, ou vide si l'activation n'a pas pu être demandée
     *         (service indisponible) : l'élément reste alors RECU.
     */
    Optional<EtatElement> demanderActivation(ClientId clientId, CommandeId commandeId,
                                             ElementCommande element, Instant acceptedAt);

    /** État de chaque élément selon son service dans l'inventaire ; vide si l'inventaire est indisponible. */
    Optional<Map<ElementId, EtatElement>> etatsDesElements(ClientId clientId, CommandeId commandeId);
}
