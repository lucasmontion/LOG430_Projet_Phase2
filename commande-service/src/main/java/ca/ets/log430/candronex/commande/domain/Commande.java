package ca.ets.log430.candronex.commande.domain;

import ca.ets.log430.candronex.commun.domaine.ClientId;
import ca.ets.log430.candronex.commun.domaine.DonneeInvalideException;

import java.util.List;
import java.util.Objects;

/**
 * Racine de l'agrégat Commande. Classe Java pure (ADR-0002).
 * On ne modifie jamais un élément directement : on passe par la commande,
 * ce qui garantit que l'état global reste cohérent avec les éléments.
 */
public class Commande {
    private final CommandeId id;
    private final ClientId clientId;
    private final CleIdempotence cleIdempotence;
    private final List<ElementCommande> elements;

    private Commande(CommandeId id, ClientId clientId, CleIdempotence cle, List<ElementCommande> elements) {
        this.id = Objects.requireNonNull(id, "id");
        this.clientId = Objects.requireNonNull(clientId, "clientId");
        this.cleIdempotence = Objects.requireNonNull(cle, "cleIdempotence");
        this.elements = List.copyOf(elements);
    }

    /** UC-04 : règles de création d'une nouvelle commande. */
    public static Commande creer(ClientId clientId, CleIdempotence cle, List<ElementCommande> elements) {
        if (elements == null || elements.isEmpty()) {
            throw new DonneeInvalideException("COMMANDE_VIDE", "Une commande doit contenir au moins un élément");
        }
        for (int i = 0; i < elements.size(); i++) {
            for (int j = i + 1; j < elements.size(); j++) {
                if (elements.get(i).viseLeMemeServiceQue(elements.get(j))) {
                    throw new DonneeInvalideException("ELEMENT_EN_DOUBLE",
                            "Deux éléments demandent le même service pour le même drone");
                }
            }
        }
        return new Commande(CommandeId.nouveau(), clientId, cle, elements);
    }

    /** Recrée une commande existante depuis la persistance. */
    public static Commande reconstituer(CommandeId id, ClientId clientId, CleIdempotence cle,
                                        List<ElementCommande> elements) {
        return new Commande(id, clientId, cle, elements);
    }

    /** État global dérivé des éléments (§6.2) : jamais stocké, toujours recalculé. */
    public EtatCommande etat() {
        return calculerEtat(elements);
    }

    /**
     * Règles appliquées dans l'ordre, la première qui s'applique gagne.
     * Un échec n'est jamais masqué derrière un succès : C2 en ECHEC + IMAGERIE ACTIF = PARTIELLE.
     */
    static EtatCommande calculerEtat(List<ElementCommande> elements) {
        if (elements.stream().allMatch(e -> e.etat() == EtatElement.RECU)) return EtatCommande.RECUE;
        if (elements.stream().anyMatch(e -> e.etat().estEnTraitement())) return EtatCommande.EN_COURS;
        if (elements.stream().allMatch(e -> e.etat() == EtatElement.ACTIF)) return EtatCommande.COMPLETEE;
        if (elements.stream().allMatch(e -> e.etat().estRetire())) return EtatCommande.ANNULEE;
        boolean unActif = elements.stream().anyMatch(e -> e.etat() == EtatElement.ACTIF);
        boolean unEchecOuRetire = elements.stream()
                .anyMatch(e -> e.etat() == EtatElement.ECHEC || e.etat().estRetire());
        if (unActif && unEchecOuRetire) return EtatCommande.PARTIELLE;
        return EtatCommande.ECHOUEE;
    }

    public boolean appartientA(ClientId client) {
        return clientId.equals(client);
    }

    public CommandeId id() { return id; }
    public ClientId clientId() { return clientId; }
    public CleIdempotence cleIdempotence() { return cleIdempotence; }
    public List<ElementCommande> elements() { return elements; }
}
