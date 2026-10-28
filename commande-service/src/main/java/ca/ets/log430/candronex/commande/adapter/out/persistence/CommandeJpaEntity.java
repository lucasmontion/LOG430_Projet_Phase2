package ca.ets.log430.candronex.commande.adapter.out.persistence;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Modèle de persistance de la commande (base de commande-service).
 * Pas de colonne d'état global : il est dérivé des éléments, jamais stocké.
 */
@Entity
@Table(name = "commande")
public class CommandeJpaEntity {
    @Id
    private UUID id;

    @Column(name = "client_id", nullable = false)
    private String clientId;

    @Column(name = "cle_idempotence", nullable = false)
    private String cleIdempotence;

    // Relation JPA autorisée : à l'INTÉRIEUR de l'agrégat (ADR-0002)
    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("rang ASC")
    private List<ElementCommandeJpaEntity> elements = new ArrayList<>();

    protected CommandeJpaEntity() {}

    public CommandeJpaEntity(UUID id, String clientId, String cleIdempotence) {
        this.id = id;
        this.clientId = clientId;
        this.cleIdempotence = cleIdempotence;
    }

    public void ajouterElement(ElementCommandeJpaEntity element) {
        elements.add(element);
        element.rattacherA(this);
    }

    public UUID getId() { return id; }
    public String getClientId() { return clientId; }
    public String getCleIdempotence() { return cleIdempotence; }
    public List<ElementCommandeJpaEntity> getElements() { return elements; }
}
