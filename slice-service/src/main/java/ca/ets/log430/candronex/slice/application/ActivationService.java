package ca.ets.log430.candronex.slice.application;

import ca.ets.log430.candronex.commun.domaine.ClientId;
import ca.ets.log430.candronex.slice.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Qualifier;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executor;

/**
 * Service applicatif de UC-06 (contextes Activation et Inventaire).
 * demander() est synchrone et rapide (création du service, réponse 202) ;
 * activer() s'exécute en arrière-plan : IMSI, paramètres réseau, appel au réseau, résultat.
 * Les règles d'état restent dans l'agrégat Service ; ce service ne fait qu'orchestrer.
 * (Annotation Spring écrite en entier : « Service » désigne ici l'agrégat du domaine.)
 */
@org.springframework.stereotype.Service
public class ActivationService {
    private static final Logger log = LoggerFactory.getLogger(ActivationService.class);
    static final String CLE_MDC_COMMANDE = "commandeId";

    private final ServiceRepository inventaire;
    private final IdentiteReseauPort identiteReseau;
    private final NetworkProvisioningPort reseau;
    private final Executor arrierePlan;
    private final Clock horloge;

    public ActivationService(ServiceRepository inventaire, IdentiteReseauPort identiteReseau,
                             NetworkProvisioningPort reseau,
                             @Qualifier("executeurActivation") Executor arrierePlan, Clock horloge) {
        this.inventaire = inventaire;
        this.identiteReseau = identiteReseau;
        this.reseau = reseau;
        this.arrierePlan = arrierePlan;
        this.horloge = horloge;
    }

    /** UC-06, étapes 1-2 : accepter la demande (idempotente) et lancer l'activation en arrière-plan. */
    public ResultatDemande demander(DemanderActivationCommand demande) {
        OrigineCommande origine = new OrigineCommande(demande.commandeId(), demande.elementId());
        MDC.put(CLE_MDC_COMMANDE, demande.commandeId().toString());   // retiré en fin de requête par CorrelationIdFilter
        log.info("[application] ActivationService.demander commandeId={} elementId={} typeService={}",
                demande.commandeId(), demande.elementId(), demande.typeService());

        // Flux 1b : demande répétée -> le service existant, sans nouvelle configuration réseau
        Optional<Service> existant = inventaire.trouverParOrigine(origine);
        if (existant.isPresent()) {
            log.info("[application] rejeu détecté, service existant serviceId={} etat={}",
                    existant.get().id().valeur(), existant.get().etat());
            return new ResultatDemande(existant.get(), false);
        }

        Service nouveau = Service.demander(origine, new ClientId(demande.clientId()),
                new DroneId(demande.droneId()), demande.typeService(),
                new ConfigurationSlice(demande.sst(), demande.sd(), demande.dnn(), demande.fiveQi(), demande.arp(),
                        demande.ambrMontant(), demande.ambrDescendant()),
                demande.acceptedAt());

        Service enregistre = inventaire.enregistrerSiAbsent(nouveau);
        if (!enregistre.id().equals(nouveau.id())) {   // une demande identique simultanée a gagné
            return new ResultatDemande(enregistre, false);
        }
        log.info("[application] service créé serviceId={} etat={}", enregistre.id().valeur(), enregistre.etat());

        ServiceId id = enregistre.id();
        arrierePlan.execute(() -> activer(id));
        return new ResultatDemande(enregistre, true);
    }

    /** UC-06, étapes 3-8, en arrière-plan. Chaque issue mène à un état explicite : jamais d'état incohérent. */
    void activer(ServiceId id) {
        Service service = inventaire.trouver(id).orElseThrow(() -> new ServiceIntrouvableException(id));
        MDC.put(CLE_MDC_COMMANDE, service.origine().commandeId().toString());
        try {
            // Étape 3 : droneId -> IMSI, résolu maintenant (jamais figé dans la commande)
            Optional<IdentiteReseau> abonne;
            try {
                abonne = identiteReseau.resoudre(service.clientId(), service.droneId());
            } catch (IdentiteReseauIndisponibleException e) {
                terminerEnEchec(service, e.getMessage());   // rien n'a été appliqué : échec certain
                return;
            }
            if (abonne.isEmpty()) {
                terminerEnEchec(service, "Drone introuvable ou non actif");   // flux 3a
                return;
            }

            // Étapes 4-5 : seule porte vers le réseau, franchie une seule fois (règle portée par l'agrégat)
            service.demarrerActivation();
            inventaire.sauvegarder(service);
            log.info("[application] activation démarrée serviceId={} snssai={}",
                    service.id().valeur(), service.configuration().snssai());

            ResultatProvisionnement resultat = reseau.appliquer(abonne.get(), service.configuration());

            // Étapes 6-7 : traduire la réponse du réseau en transition d'état
            switch (resultat.issue()) {
                case SUCCES -> service.confirmerActivation(horloge.instant());
                case ECHEC -> service.echouer(resultat.cause());
                case INDETERMINE -> service.marquerIndetermine(resultat.cause());
            }
            inventaire.sauvegarder(service);
            log.info("[application] résultat de l'activation serviceId={} etat={} delai={} cause={}",
                    service.id().valeur(), service.etat(),
                    service.delaiActivation().map(d -> d.toMillis() + " ms").orElse("-"),
                    service.causeEchec().orElse("-"));
        } catch (RuntimeException e) {
            log.error("[application] erreur inattendue pendant l'activation serviceId={}", id.valeur(), e);
        } finally {
            MDC.remove(CLE_MDC_COMMANDE);
        }
    }

    private void terminerEnEchec(Service service, String cause) {
        service.echouer(cause);
        inventaire.sauvegarder(service);
        log.warn("[application] activation en échec serviceId={} cause={}", service.id().valeur(), cause);
    }

    /** Consultation d'un service, toujours filtrée par client. */
    public Service consulter(String clientId, String serviceId) {
        ServiceId id = ServiceId.depuis(serviceId);
        return inventaire.trouverPourClient(new ClientId(clientId), id)
                .orElseThrow(() -> new ServiceIntrouvableException(id));
    }

    /** Services d'une commande (lus par commande-service pour calculer l'état global). */
    public List<Service> servicesDeLaCommande(String clientId, UUID commandeId) {
        return inventaire.trouverParCommande(new ClientId(clientId), commandeId);
    }
}
