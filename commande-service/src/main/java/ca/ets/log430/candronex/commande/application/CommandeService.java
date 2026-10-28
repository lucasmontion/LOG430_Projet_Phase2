package ca.ets.log430.candronex.commande.application;

import ca.ets.log430.candronex.catalogue.api.CatalogueFacade;
import ca.ets.log430.candronex.catalogue.api.SpecificationDto;
import ca.ets.log430.candronex.commande.domain.*;
import ca.ets.log430.candronex.commun.domaine.ClientId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service applicatif du module commande. Coordonne UC-04 :
 * idempotence, vérification du drone (module drone), copie des caractéristiques
 * (module catalogue), puis création par l'agrégat.
 * Les autres modules ne sont appelés que par leur façade publique.
 */
@Service
public class CommandeService {
    private static final Logger log = LoggerFactory.getLogger(CommandeService.class);

    private final CommandeRepository commandeRepository;
    private final VerificationDronePort verificationDrone;
    private final CatalogueFacade catalogueFacade;

    public CommandeService(CommandeRepository commandeRepository,
                           
                           VerificationDronePort verificationDrone,
                           CatalogueFacade catalogueFacade) {
        this.commandeRepository = commandeRepository;
        this.verificationDrone = verificationDrone;
        this.catalogueFacade = catalogueFacade;
    }

    /** UC-04 : commander les services d'un drone. */
    public ResultatCommande passer(PasserCommandeCommand commande) {
        ClientId clientId = new ClientId(commande.clientId());
        CleIdempotence cle = new CleIdempotence(commande.cleIdempotence());
        log.info("[application] CommandeService.passer clientId={} cle={}", clientId.valeur(), cle.valeur());

        // Flux 1a : requête répétée -> on retourne la commande existante, sans rien recréer
        Optional<Commande> existante = commandeRepository.trouverParCle(clientId, cle);
        if (existante.isPresent()) {
            log.info("[application] rejeu détecté, commande existante commandeId={}", existante.get().id().valeur());
            return new ResultatCommande(existante.get(), false);
        }

        List<PasserCommandeCommand.Element> demandes =
                commande.elements() == null ? List.of() : commande.elements();
        List<ElementCommande> elements = demandes.stream()
                .map(demande -> creerElement(clientId, demande))
                .toList();

        Commande nouvelle = Commande.creer(clientId, cle, elements);
        commandeRepository.sauvegarder(nouvelle);
        log.info("[application] commande créée commandeId={} elements={} etat={}",
                nouvelle.id().valeur(), elements.size(), nouvelle.etat());
        return new ResultatCommande(nouvelle, true);
    }

    private ElementCommande creerElement(ClientId clientId, PasserCommandeCommand.Element demande) {
        DroneId droneId = new DroneId(demande.droneId());
        TypeService type = new TypeService(demande.typeService());

      
        DroneVerifie drone = verificationDrone.verifier(clientId, droneId)
                .orElseThrow(() -> new DroneNonTrouveException(droneId));
        if (!drone.estActif()) {
            throw new DroneNonEligibleException(droneId, drone.statut());
        }

  
        SpecificationDto spec = catalogueFacade.trouverSpecification(type.valeur())
                .orElseThrow(() -> new ServiceInconnuException(type));
        CaracteristiquesFigees caracteristiques = new CaracteristiquesFigees(
                spec.sst(), spec.sd(), spec.dnn(), spec.fiveQi(), spec.arp(),
                spec.ambrMontant(), spec.ambrDescendant());

        return ElementCommande.nouveau(droneId, type, caracteristiques);
    }

    /** Consultation d'une commande, toujours filtrée par client. */
    public Commande consulter(String clientId, String commandeId) {
        CommandeId id = CommandeId.depuis(commandeId);
        return commandeRepository.trouver(new ClientId(clientId), id)
                .orElseThrow(() -> new CommandeIntrouvableException(id));
    }
}
