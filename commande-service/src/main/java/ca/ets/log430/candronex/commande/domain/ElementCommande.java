package ca.ets.log430.candronex.commande.domain;

import java.util.Objects;

/**
 * Entité interne à l'agrégat Commande : n'existe qu'à travers sa commande.
 * Chaque élément a son propre état, indépendant des autres.
 */
public class ElementCommande {
    private final ElementId id;
    private final DroneId droneId;
    private final TypeService typeService;
    private final CaracteristiquesFigees caracteristiques;
    private EtatElement etat;

    private ElementCommande(ElementId id, DroneId droneId, TypeService typeService,
                            CaracteristiquesFigees caracteristiques, EtatElement etat) {
        this.id = Objects.requireNonNull(id, "id");
        this.droneId = Objects.requireNonNull(droneId, "droneId");
        this.typeService = Objects.requireNonNull(typeService, "typeService");
        this.caracteristiques = Objects.requireNonNull(caracteristiques, "caracteristiques");
        this.etat = Objects.requireNonNull(etat, "etat");
    }

    /** Nouvel élément : toujours dans l'état initial RECU. */
    public static ElementCommande nouveau(DroneId droneId, TypeService typeService,
                                          CaracteristiquesFigees caracteristiques) {
        return new ElementCommande(ElementId.nouveau(), droneId, typeService, caracteristiques, EtatElement.RECU);
    }

    public static ElementCommande reconstituer(ElementId id, DroneId droneId, TypeService typeService,
                                               CaracteristiquesFigees caracteristiques, EtatElement etat) {
        return new ElementCommande(id, droneId, typeService, caracteristiques, etat);
    }

    boolean viseLeMemeServiceQue(ElementCommande autre) {
        return droneId.equals(autre.droneId) && typeService.equals(autre.typeService);
    }

    public ElementId id() { return id; }
    public DroneId droneId() { return droneId; }
    public TypeService typeService() { return typeService; }
    public CaracteristiquesFigees caracteristiques() { return caracteristiques; }
    public EtatElement etat() { return etat; }
}
