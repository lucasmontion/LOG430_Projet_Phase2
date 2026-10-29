package ca.ets.log430.candronex.slice.domain;

import ca.ets.log430.candronex.commun.domaine.ClientId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Port de sortie : l'inventaire des services. Toute lecture destinée au client est filtrée par client. */
public interface ServiceRepository {

    /**
     * Enregistre un NOUVEAU service, sauf si un service existe déjà pour la même origine (commande, élément).
     * Garantit l'idempotence même si deux demandes identiques arrivent en même temps.
     * @return le service enregistré, ou celui qui existait déjà
     */
    Service enregistrerSiAbsent(Service service);

    /** Met à jour un service existant (changement d'état). */
    void sauvegarder(Service service);

    Optional<Service> trouver(ServiceId id);

    Optional<Service> trouverParOrigine(OrigineCommande origine);

    Optional<Service> trouverPourClient(ClientId clientId, ServiceId id);

    List<Service> trouverParCommande(ClientId clientId, UUID commandeId);
}
