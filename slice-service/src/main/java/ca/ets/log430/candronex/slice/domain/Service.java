package ca.ets.log430.candronex.slice.domain;

import ca.ets.log430.candronex.commun.domaine.ClientId;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * Racine de l'agrégat Service : un service de connectivité instancié pour un drone (inventaire, TMF638).
 * Il n'y a pas d'agrégat Activation : activer, c'est faire parcourir à ce service son cycle de vie (TMF640).
 * Classe Java pure : chaque transition du diagramme d'état (§6.2) est une méthode, toute autre est refusée.
 * L'heure est fournie par l'appelant : le domaine ne dépend d'aucune horloge, ce qui le rend testable.
 */
public class Service {
    private final ServiceId id;
    private final OrigineCommande origine;
    private final ClientId clientId;
    private final DroneId droneId;
    private final String typeService;
    private final ConfigurationSlice configuration;
    private final Instant acceptedAt;
    private EtatService etat;
    private Instant activatedAt;
    private String causeEchec;

    private Service(ServiceId id, OrigineCommande origine, ClientId clientId, DroneId droneId, String typeService,
                    ConfigurationSlice configuration, Instant acceptedAt, EtatService etat,
                    Instant activatedAt, String causeEchec) {
        this.id = Objects.requireNonNull(id, "id");
        this.origine = Objects.requireNonNull(origine, "origine");
        this.clientId = Objects.requireNonNull(clientId, "clientId");
        this.droneId = Objects.requireNonNull(droneId, "droneId");
        this.typeService = Objects.requireNonNull(typeService, "typeService");
        this.configuration = Objects.requireNonNull(configuration, "configuration");
        this.acceptedAt = Objects.requireNonNull(acceptedAt, "acceptedAt");
        this.etat = Objects.requireNonNull(etat, "etat");
        this.activatedAt = activatedAt;
        this.causeEchec = causeEchec;
    }

    /** [*] -> EN_ATTENTE : demande d'activation acceptée. acceptedAt vient de la commande (mesure du P95). */
    public static Service demander(OrigineCommande origine, ClientId clientId, DroneId droneId, String typeService,
                                   ConfigurationSlice configuration, Instant acceptedAt) {
        return new Service(ServiceId.nouveau(), origine, clientId, droneId, typeService, configuration,
                acceptedAt, EtatService.EN_ATTENTE, null, null);
    }

    /** Recrée un service existant depuis la persistance, sans réappliquer les règles. */
    public static Service reconstituer(ServiceId id, OrigineCommande origine, ClientId clientId, DroneId droneId,
                                       String typeService, ConfigurationSlice configuration, Instant acceptedAt,
                                       EtatService etat, Instant activatedAt, String causeEchec) {
        return new Service(id, origine, clientId, droneId, typeService, configuration,
                acceptedAt, etat, activatedAt, causeEchec);
    }

    /**
     * EN_ATTENTE -> EN_ACTIVATION, juste avant l'appel au réseau.
     * Seule transition qui autorise un appel au réseau, et possible une seule fois : pas de configuration en double.
     */
    public void demarrerActivation() {
        exigerEtat(EtatService.EN_ATTENTE, "démarrer l'activation");
        etat = EtatService.EN_ACTIVATION;
    }

    /** EN_ACTIVATION -> ACTIF : le réseau a confirmé la configuration. */
    public void confirmerActivation(Instant maintenant) {
        exigerEtat(EtatService.EN_ACTIVATION, "confirmer l'activation");
        etat = EtatService.ACTIF;
        activatedAt = Objects.requireNonNull(maintenant, "maintenant");
    }

    /**
     * EN_ATTENTE -> ECHEC (drone introuvable) ou EN_ACTIVATION -> ECHEC (erreur réseau, réseau injoignable).
     * Échec certain : aucune configuration n'a été appliquée.
     */
    public void echouer(String cause) {
        if (etat != EtatService.EN_ATTENTE && etat != EtatService.EN_ACTIVATION) {
            throw new TransitionInvalideException(etat, "faire échouer");
        }
        etat = EtatService.ECHEC;
        causeEchec = Objects.requireNonNull(cause, "cause");
    }

    /** EN_ACTIVATION -> ECHEC_INDETERMINE : délai dépassé, la configuration a peut-être été appliquée. */
    public void marquerIndetermine(String cause) {
        exigerEtat(EtatService.EN_ACTIVATION, "marquer comme indéterminé");
        etat = EtatService.ECHEC_INDETERMINE;
        causeEchec = Objects.requireNonNull(cause, "cause");
    }

    /** Délai d'activation (activatedAt - acceptedAt), connu seulement une fois le service actif. */
    public Optional<Duration> delaiActivation() {
        return Optional.ofNullable(activatedAt).map(fin -> Duration.between(acceptedAt, fin));
    }

    public boolean appartientA(ClientId client) {
        return clientId.equals(client);
    }

    private void exigerEtat(EtatService attendu, String operation) {
        if (etat != attendu) {
            throw new TransitionInvalideException(etat, operation);
        }
    }

    public ServiceId id() { return id; }
    public OrigineCommande origine() { return origine; }
    public ClientId clientId() { return clientId; }
    public DroneId droneId() { return droneId; }
    public String typeService() { return typeService; }
    public ConfigurationSlice configuration() { return configuration; }
    public Instant acceptedAt() { return acceptedAt; }
    public EtatService etat() { return etat; }
    public Optional<Instant> activatedAt() { return Optional.ofNullable(activatedAt); }
    public Optional<String> causeEchec() { return Optional.ofNullable(causeEchec); }
}
