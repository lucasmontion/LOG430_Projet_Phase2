package ca.ets.log430.candronex.commande.application;

import ca.ets.log430.candronex.catalogue.api.CatalogueFacade;
import ca.ets.log430.candronex.catalogue.api.SpecificationDto;
import ca.ets.log430.candronex.commande.domain.*;
import ca.ets.log430.candronex.commun.domaine.DonneeInvalideException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires de UC-04 : scénario principal et flux alternatifs.
 * Les façades des autres modules sont simulées (Mockito) : on teste UC-04 seul.
 */
class CommandeServiceTest {

    private CommandeRepositoryEnMemoire repository;
    private CommandeService service;

    @BeforeEach
    void preparer() {
        repository = new CommandeRepositoryEnMemoire();
        CatalogueFacade catalogueFacade = mock(CatalogueFacade.class);
        VerificationDronePort drones = (client, drone) -> switch (drone.valeur()) {
            case "DRONE-001" -> client.valeur().equals("CLIENT-001")
                    ? Optional.of(new DroneVerifie(drone, "ACTIF")) : Optional.empty();
            case "DRONE-SUSPENDU" -> Optional.of(new DroneVerifie(drone, "SUSPENDU"));
            default -> Optional.empty();
        };
        service = new CommandeService(repository, drones, catalogueFacade);
        service = new CommandeService(repository, drones, catalogueFacade);



        when(catalogueFacade.trouverSpecification(anyString())).thenReturn(Optional.empty());
        when(catalogueFacade.trouverSpecification("C2")).thenReturn(Optional.of(
                new SpecificationDto("C2", "C&C Connectivity", 2, "000001", "c2", 7, 2, "20 Mbps", "20 Mbps")));
        when(catalogueFacade.trouverSpecification("IMAGERIE")).thenReturn(Optional.of(
                new SpecificationDto("IMAGERIE", "Imagery Connectivity", 1, "000002", "imagery", 9, 8, "500 Mbps", "100 Mbps")));
    }

    private PasserCommandeCommand commande(String cle, PasserCommandeCommand.Element... elements) {
        return new PasserCommandeCommand("CLIENT-001", cle, List.of(elements));
    }

    private PasserCommandeCommand.Element el(String drone, String type) {
        return new PasserCommandeCommand.Element(drone, type);
    }

    @Test
    void creeUneCommandeC2EtImagerieAvecLesCaracteristiquesCopiees() { // scénario de démo de l'énoncé
        ResultatCommande resultat = service.passer(commande("cle-1", el("DRONE-001", "C2"), el("DRONE-001", "IMAGERIE")));

        Commande commande = resultat.commande();
        assertThat(resultat.nouvelle()).isTrue();
        assertThat(commande.etat()).isEqualTo(EtatCommande.RECUE);
        assertThat(commande.elements()).hasSize(2);
        assertThat(commande.elements().get(0).caracteristiques().arp()).isEqualTo(2);
        assertThat(commande.elements().get(1).caracteristiques().ambrMontant()).isEqualTo("500 Mbps");
    }

    @Test
    void uneRequeteRepeteeRetourneLaCommandeExistanteSansEnCreerUneAutre() { // flux 1a
        Commande premiere = service.passer(commande("cle-1", el("DRONE-001", "C2"))).commande();

        ResultatCommande rejeu = service.passer(commande("cle-1", el("DRONE-001", "C2")));

        assertThat(rejeu.nouvelle()).isFalse();
        assertThat(rejeu.commande().id()).isEqualTo(premiere.id());
        assertThat(repository.commandes).hasSize(1);
    }

    @Test
    void refuseUneCommandeSansCleIdempotence() {
        assertThatThrownBy(() -> service.passer(commande(null, el("DRONE-001", "C2"))))
                .isInstanceOf(DonneeInvalideException.class);
    }

    @Test
    void refuseUneCommandeSansElement() { // flux 1b
        assertThatThrownBy(() -> service.passer(commande("cle-1")))
                .isInstanceOf(DonneeInvalideException.class);
    }

    @Test
    void refuseUnDroneInexistantOuDUnAutreClient() { // flux 2a
        assertThatThrownBy(() -> service.passer(commande("cle-1", el("DRONE-INCONNU", "C2"))))
                .isInstanceOf(DroneNonTrouveException.class);
        assertThat(repository.commandes).isEmpty();
    }

    @Test
    void refuseUnDroneNonActif() { // flux 2b
        assertThatThrownBy(() -> service.passer(commande("cle-1", el("DRONE-SUSPENDU", "C2"))))
                .isInstanceOf(DroneNonEligibleException.class);
    }

    @Test
    void refuseUnServiceAbsentDuCatalogue() { // flux 3a
        assertThatThrownBy(() -> service.passer(commande("cle-1", el("DRONE-001", "SATELLITE"))))
                .isInstanceOf(ServiceInconnuException.class);
        assertThat(repository.commandes).isEmpty(); // tout ou rien
    }

    @Test
    void neMontrePasLaCommandeDUnAutreClient() {
        Commande commande = service.passer(commande("cle-1", el("DRONE-001", "C2"))).commande();

        assertThatThrownBy(() -> service.consulter("CLIENT-002", commande.id().valeur().toString()))
                .isInstanceOf(CommandeIntrouvableException.class);
    }

    
}
