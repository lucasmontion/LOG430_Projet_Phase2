package ca.ets.log430.candronex.commande.application;

import ca.ets.log430.candronex.catalogue.api.CatalogueFacade;
import ca.ets.log430.candronex.catalogue.api.SpecificationDto;
import ca.ets.log430.candronex.commande.domain.*;
import ca.ets.log430.candronex.commun.domaine.ClientId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service applicatif de la commande. Coordonne UC-04 puis déclenche UC-06 :
 * idempotence, vérification du drone (drone-service, par port), copie des caractéristiques
 * (catalogue), création par l'agrégat, puis demande d'activation de chaque élément (slice-service, par port).
 */
@Service
public class CommandeService {
    private static final Logger log = LoggerFactory.getLogger(CommandeService.class);
    static final String CLE_MDC_COMMANDE = "commandeId";   // retiré en fin de requête par CorrelationIdFilter

    private final CommandeRepository commandeRepository;
    private final VerificationDronePort verificationDrone;
    private final CatalogueFacade catalogueFacade;
    private final ActivationPort activation;
    private final Clock horloge;

    public CommandeService(CommandeRepository commandeRepository,
                           VerificationDronePort verificationDrone,
                           CatalogueFacade catalogueFacade,
                           ActivationPort activation,
                           Clock horloge) {
        this.commandeRepository = commandeRepository;
        this.verificationDrone = verificationDrone;
        this.catalogueFacade = catalogueFacade;
        this.activation = activation;
        this.horloge = horloge;
    }

    /** UC-04 : commander les services d'un drone ; UC-06 est déclenché pour chaque élément. */
    public ResultatCommande passer(PasserCommandeCommand commande) {
        ClientId clientId = new ClientId(commande.clientId());
        CleIdempotence cle = new CleIdempotence(commande.cleIdempotence());
        log.info("[application] CommandeService.passer clientId={} cle={}", clientId.valeur(), cle.valeur());

        // Flux 1a : requête répétée -> la commande existante, sans rien recréer.
        // La demande d'activation est renvoyée : elle est idempotente côté slice-service,
        // et permet de rattraper une activation qui n'avait pas pu être demandée.
        Optional<Commande> existante = commandeRepository.trouverParCle(clientId, cle);
        if (existante.isPresent()) {
            MDC.put(CLE_MDC_COMMANDE, existante.get().id().valeur().toString());
            log.info("[application] rejeu détecté, commande existante commandeId={}", existante.get().id().valeur());
            demanderActivations(existante.get());
            return new ResultatCommande(existante.get(), false);
        }

        List<PasserCommandeCommand.Element> demandes =
                commande.elements() == null ? List.of() : commande.elements();
        List<ElementCommande> elements = demandes.stream()
                .map(demande -> creerElement(clientId, demande))
                .toList();

        Commande nouvelle = Commande.creer(clientId, cle, elements);
        MDC.put(CLE_MDC_COMMANDE, nouvelle.id().valeur().toString());
        commandeRepository.sauvegarder(nouvelle);
        log.info("[application] commande créée commandeId={} elements={} etat={}",
                nouvelle.id().valeur(), elements.size(), nouvelle.etat());

        demanderActivations(nouvelle);   // UC-06, après l'enregistrement : la commande existe même si slice est indisponible
        return new ResultatCommande(nouvelle, true);
    }

    /** UC-06, étape 1 : demander l'activation de chaque élément ; acceptedAt marque le début de la mesure du P95. */
    private void demanderActivations(Commande commande) {
        Instant acceptedAt = horloge.instant();
        Map<ElementId, EtatElement> etats = new HashMap<>();
        for (ElementCommande element : commande.elements()) {
            activation.demanderActivation(commande.clientId(), commande.id(), element, acceptedAt)
                    .ifPresentOrElse(
                            etat -> etats.put(element.id(), etat),
                            () -> log.warn("[application] activation non demandée (slice-service indisponible) elementId={}",
                                    element.id().valeur()));
        }
        commande.refleterEtatsDesServices(etats);
    }

    private ElementCommande creerElement(ClientId clientId, PasserCommandeCommand.Element demande) {
        DroneId droneId = new DroneId(demande.droneId());
        TypeService type = new TypeService(demande.typeService());

        // Flux 2a et 2b : le drone doit exister, appartenir au client et être actif (drone-service)
        DroneVerifie drone = verificationDrone.verifier(clientId, droneId)
                .orElseThrow(() -> new DroneNonTrouveException(droneId));
        if (!drone.estActif()) {
            throw new DroneNonEligibleException(droneId, drone.statut());
        }

        // Flux 3a : le service doit exister ; ses caractéristiques sont COPIÉES (figées)
        SpecificationDto spec = catalogueFacade.trouverSpecification(type.valeur())
                .orElseThrow(() -> new ServiceInconnuException(type));
        CaracteristiquesFigees caracteristiques = new CaracteristiquesFigees(
                spec.sst(), spec.sd(), spec.dnn(), spec.fiveQi(), spec.arp(),
                spec.ambrMontant(), spec.ambrDescendant());

        return ElementCommande.nouveau(droneId, type, caracteristiques);
    }

    /** Consultation d'une commande, toujours filtrée par client ; l'état des éléments vient de l'inventaire. */
    public Commande consulter(String clientId, String commandeId) {
        CommandeId id = CommandeId.depuis(commandeId);
        MDC.put(CLE_MDC_COMMANDE, id.valeur().toString());
        Commande commande = commandeRepository.trouver(new ClientId(clientId), id)
                .orElseThrow(() -> new CommandeIntrouvableException(id));
        activation.etatsDesElements(commande.clientId(), commande.id())
                .ifPresentOrElse(commande::refleterEtatsDesServices,
                        () -> log.warn("[application] inventaire indisponible : état des éléments non actualisé commandeId={}",
                                id.valeur()));
        return commande;
    }
}
