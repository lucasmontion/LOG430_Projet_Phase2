package ca.ets.log430.candronex.slice.domain;

import ca.ets.log430.candronex.commun.domaine.ClientId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Le diagramme d'état du §6.2, transformé en tests : chaque flèche autorisée, et les transitions interdites. */
class ServiceTest {

    static final ConfigurationSlice C2 = new ConfigurationSlice(2, "000001", "c2", 7, 2, "20 Mbps", "20 Mbps");
    static final Instant ACCEPTEE = Instant.parse("2026-10-29T12:00:00Z");

    private Service nouveauService() {
        return Service.demander(new OrigineCommande(UUID.randomUUID(), UUID.randomUUID()),
                new ClientId("inspectra-demo"), new DroneId("DRONE-001"), "C2", C2, ACCEPTEE);
    }

    /** Amène un service à l'état voulu en suivant uniquement des transitions autorisées. */
    private Service serviceA(EtatService etat) {
        Service s = nouveauService();
        switch (etat) {
            case EN_ATTENTE -> { }
            case EN_ACTIVATION -> s.demarrerActivation();
            case ACTIF -> { s.demarrerActivation(); s.confirmerActivation(ACCEPTEE.plusMillis(800)); }
            case ECHEC -> { s.demarrerActivation(); s.echouer("échec simulé"); }
            case ECHEC_INDETERMINE -> { s.demarrerActivation(); s.marquerIndetermine("délai dépassé"); }
        }
        return s;
    }

    @Test
    void uneDemandeAccepteeCommenceEnAttente() { // [*] -> EN_ATTENTE
        Service s = nouveauService();

        assertThat(s.etat()).isEqualTo(EtatService.EN_ATTENTE);
        assertThat(s.acceptedAt()).isEqualTo(ACCEPTEE);
        assertThat(s.activatedAt()).isEmpty();
    }

    @Test
    void scenarioNominalJusquaActifAvecDelaiDActivation() { // EN_ATTENTE -> EN_ACTIVATION -> ACTIF
        Service s = nouveauService();

        s.demarrerActivation();
        assertThat(s.etat()).isEqualTo(EtatService.EN_ACTIVATION);

        s.confirmerActivation(ACCEPTEE.plusMillis(1200));
        assertThat(s.etat()).isEqualTo(EtatService.ACTIF);
        assertThat(s.activatedAt()).contains(ACCEPTEE.plusMillis(1200));
        assertThat(s.delaiActivation()).contains(Duration.ofMillis(1200)); // activatedAt - acceptedAt (P95)
    }

    @Test
    void droneIntrouvableFaitEchouerAvantToutAppelAuReseau() { // EN_ATTENTE -> ECHEC
        Service s = nouveauService();

        s.echouer("drone introuvable");

        assertThat(s.etat()).isEqualTo(EtatService.ECHEC);
        assertThat(s.causeEchec()).contains("drone introuvable");
    }

    @Test
    void erreurDuReseauDonneUnEchecCertainAvecSaCause() { // EN_ACTIVATION -> ECHEC
        Service s = serviceA(EtatService.EN_ACTIVATION);

        s.echouer("échec simulé");

        assertThat(s.etat()).isEqualTo(EtatService.ECHEC);
        assertThat(s.causeEchec()).contains("échec simulé");
        assertThat(s.delaiActivation()).isEmpty(); // jamais actif : compte comme dépassant 5 s dans la mesure
    }

    @Test
    void delaiDepasseDonneUnEchecIndetermine() { // EN_ACTIVATION -> ECHEC_INDETERMINE
        Service s = serviceA(EtatService.EN_ACTIVATION);

        s.marquerIndetermine("délai dépassé");

        assertThat(s.etat()).isEqualTo(EtatService.ECHEC_INDETERMINE);
    }

    @ParameterizedTest
    @EnumSource(value = EtatService.class, names = "EN_ATTENTE", mode = EnumSource.Mode.EXCLUDE)
    void lActivationNePeutDemarrerQuUneSeuleFois(EtatService etat) { // pas de configuration réseau en double
        Service s = serviceA(etat);

        assertThatThrownBy(s::demarrerActivation).isInstanceOf(TransitionInvalideException.class);
        assertThat(s.etat()).isEqualTo(etat); // un refus ne modifie rien
    }

    @ParameterizedTest
    @EnumSource(value = EtatService.class, names = "EN_ACTIVATION", mode = EnumSource.Mode.EXCLUDE)
    void seulUnServiceEnActivationPeutDevenirActif(EtatService etat) {
        Service s = serviceA(etat);

        assertThatThrownBy(() -> s.confirmerActivation(ACCEPTEE.plusSeconds(1)))
                .isInstanceOf(TransitionInvalideException.class);
    }

    @ParameterizedTest
    @EnumSource(value = EtatService.class, names = {"ACTIF", "ECHEC", "ECHEC_INDETERMINE"})
    void unServiceTermineNePeutPlusEchouer(EtatService etat) {
        Service s = serviceA(etat);

        assertThatThrownBy(() -> s.echouer("autre cause")).isInstanceOf(TransitionInvalideException.class);
        assertThat(s.etat()).isEqualTo(etat);
    }

    @ParameterizedTest
    @EnumSource(value = EtatService.class, names = "EN_ACTIVATION", mode = EnumSource.Mode.EXCLUDE)
    void seulUnAppelAuReseauEnCoursPeutDevenirIndetermine(EtatService etat) {
        Service s = serviceA(etat);

        assertThatThrownBy(() -> s.marquerIndetermine("délai dépassé"))
                .isInstanceOf(TransitionInvalideException.class);
    }

    @Test
    void appartientAuClientQuiLAcommande() {
        Service s = nouveauService();

        assertThat(s.appartientA(new ClientId("inspectra-demo"))).isTrue();
        assertThat(s.appartientA(new ClientId("aerotech-demo"))).isFalse();
    }

    @Test
    void laConfigurationDetermineLeSnssai() {
        assertThat(C2.snssai()).isEqualTo("2-000001");
    }
}
