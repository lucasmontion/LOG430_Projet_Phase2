package ca.ets.log430.candronex.slice.application;

import ca.ets.log430.candronex.slice.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests unitaires de UC-06 : scénario principal et flux alternatifs.
 * L'exécuteur est synchrone (Runnable::run) : l'activation est terminée quand demander() retourne.
 */
class ActivationServiceTest {

    static final Instant ACCEPTEE = Instant.parse("2026-10-29T12:00:00Z");
    static final Instant MAINTENANT = ACCEPTEE.plusMillis(900);

    private ServiceRepositoryEnMemoire inventaire;
    private final AtomicInteger appelsAuReseau = new AtomicInteger();
    private ResultatProvisionnement reponseDuReseau;
    private Optional<IdentiteReseau> identite;
    private boolean droneServiceInjoignable;
    private ActivationService service;

    @BeforeEach
    void preparer() {
        inventaire = new ServiceRepositoryEnMemoire();
        reponseDuReseau = ResultatProvisionnement.succes();
        identite = Optional.of(new IdentiteReseau("999700000010231"));
        droneServiceInjoignable = false;

        IdentiteReseauPort drones = (client, drone) -> {
            if (droneServiceInjoignable) throw new IdentiteReseauIndisponibleException("drone-service injoignable");
            return identite;
        };
        NetworkProvisioningPort reseau = (abonne, configuration) -> {
            appelsAuReseau.incrementAndGet();
            return reponseDuReseau;
        };
        service = new ActivationService(inventaire, drones, reseau, Runnable::run,
                Clock.fixed(MAINTENANT, ZoneOffset.UTC));
    }

    private DemanderActivationCommand demande(UUID commandeId, UUID elementId) {
        return new DemanderActivationCommand("inspectra-demo", commandeId, elementId, "DRONE-001", "C2", ACCEPTEE,
                2, "000001", "c2", 7, 2, "20 Mbps", "20 Mbps");
    }

    private Service activer() {
        return service.demander(demande(UUID.randomUUID(), UUID.randomUUID())).service();
    }

    @Test
    void scenarioPrincipalLeServiceDevientActifAvecSonDelai() {
        Service s = activer();

        assertThat(s.etat()).isEqualTo(EtatService.ACTIF);
        assertThat(s.activatedAt()).contains(MAINTENANT);
        assertThat(s.delaiActivation()).contains(Duration.ofMillis(900));
        assertThat(appelsAuReseau).hasValue(1);
    }

    @Test
    void echecForceDuReseauDonneUnEchecAvecSaCause() { // flux 5a (MOCK_TAUX_ECHEC = 100 %)
        reponseDuReseau = ResultatProvisionnement.echec("Échec simulé");

        Service s = activer();

        assertThat(s.etat()).isEqualTo(EtatService.ECHEC);
        assertThat(s.causeEchec()).contains("Échec simulé");
        assertThat(s.activatedAt()).isEmpty();
    }

    @Test
    void delaiDepasseDonneUnEchecIndetermine() { // flux 5b
        reponseDuReseau = ResultatProvisionnement.indetermine("Délai d'attente du réseau dépassé");

        assertThat(activer().etat()).isEqualTo(EtatService.ECHEC_INDETERMINE);
    }

    @Test
    void droneIntrouvableOuNonActifEchoueSansAppelerLeReseau() { // flux 3a
        identite = Optional.empty();

        Service s = activer();

        assertThat(s.etat()).isEqualTo(EtatService.ECHEC);
        assertThat(appelsAuReseau).hasValue(0);
    }

    @Test
    void droneServiceInjoignableEchoueSansAppelerLeReseau() {
        droneServiceInjoignable = true;

        Service s = activer();

        assertThat(s.etat()).isEqualTo(EtatService.ECHEC);
        assertThat(s.causeEchec()).contains("drone-service injoignable");
        assertThat(appelsAuReseau).hasValue(0);
    }

    @Test
    void uneDemandeRepeteeNeCreeNiServiceNiConfigurationEnDouble() { // flux 1b, critère de l'énoncé
        UUID commande = UUID.randomUUID();
        UUID element = UUID.randomUUID();

        ResultatDemande premiere = service.demander(demande(commande, element));
        ResultatDemande rejeu = service.demander(demande(commande, element));

        assertThat(premiere.nouvelle()).isTrue();
        assertThat(rejeu.nouvelle()).isFalse();
        assertThat(rejeu.service().id()).isEqualTo(premiere.service().id());
        assertThat(inventaire.services).hasSize(1);   // une seule association slice <-> drone
        assertThat(appelsAuReseau).hasValue(1);        // une seule configuration réseau
    }

    @Test
    void deuxElementsDeLaMemeCommandeDonnentDeuxServices() { // C2 + Imagerie
        UUID commande = UUID.randomUUID();

        service.demander(demande(commande, UUID.randomUUID()));
        service.demander(demande(commande, UUID.randomUUID()));

        assertThat(service.servicesDeLaCommande("inspectra-demo", commande)).hasSize(2);
        assertThat(service.servicesDeLaCommande("aerotech-demo", commande)).isEmpty(); // isolation entre clients
    }

    @Test
    void neMontrePasLeServiceDUnAutreClient() {
        Service s = activer();

        assertThatThrownBy(() -> service.consulter("aerotech-demo", s.id().valeur().toString()))
                .isInstanceOf(ServiceIntrouvableException.class);
    }
}
