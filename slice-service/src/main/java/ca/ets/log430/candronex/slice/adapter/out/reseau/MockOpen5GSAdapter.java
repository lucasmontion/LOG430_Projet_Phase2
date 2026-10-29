package ca.ets.log430.candronex.slice.adapter.out.reseau;

import ca.ets.log430.candronex.slice.domain.ConfigurationSlice;
import ca.ets.log430.candronex.slice.domain.IdentiteReseau;
import ca.ets.log430.candronex.slice.domain.NetworkProvisioningPort;
import ca.ets.log430.candronex.slice.domain.ResultatProvisionnement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.random.RandomGenerator;

/**
 * Adaptateur de sortie SIMULÉ du NetworkProvisioningPort (Phase 2) : ne se connecte à aucun cœur 5G.
 * Simule une latence aléatoire [min, max] et un taux d'échec, réglés par variables d'environnement.
 * Si la latence tirée dépasse le délai d'attente, l'appel est abandonné : issue INDETERMINE,
 * exactement comme le ferait un adaptateur réel dont la requête au réseau expire.
 */
@Component
public class MockOpen5GSAdapter implements NetworkProvisioningPort {
    private static final Logger log = LoggerFactory.getLogger(MockOpen5GSAdapter.class);

    private final long latenceMinMs;
    private final long latenceMaxMs;
    private final double tauxEchec;
    private final long delaiAttenteMs;
    private final RandomGenerator hasard;

    @Autowired
    public MockOpen5GSAdapter(@Value("${candronex.mock.latence-min-ms}") long latenceMinMs,
                              @Value("${candronex.mock.latence-max-ms}") long latenceMaxMs,
                              @Value("${candronex.mock.taux-echec}") double tauxEchec,
                              @Value("${candronex.mock.delai-attente-ms}") long delaiAttenteMs) {
        this(latenceMinMs, latenceMaxMs, tauxEchec, delaiAttenteMs, new Random());
    }

    /** Constructeur de test : un générateur à graine fixe rend le hasard reproductible. */
    MockOpen5GSAdapter(long latenceMinMs, long latenceMaxMs, double tauxEchec, long delaiAttenteMs,
                       RandomGenerator hasard) {
        if (latenceMinMs < 0 || latenceMaxMs < latenceMinMs) {
            throw new IllegalArgumentException("Latence du mock invalide : [" + latenceMinMs + ", " + latenceMaxMs + "]");
        }
        if (tauxEchec < 0 || tauxEchec > 1) {
            throw new IllegalArgumentException("Le taux d'échec du mock doit être entre 0 et 1 : " + tauxEchec);
        }
        this.latenceMinMs = latenceMinMs;
        this.latenceMaxMs = latenceMaxMs;
        this.tauxEchec = tauxEchec;
        this.delaiAttenteMs = delaiAttenteMs;
        this.hasard = hasard;
        log.info("[adapter.out] MockOpen5GSAdapter latence=[{}, {}] ms tauxEchec={} delaiAttente={} ms",
                latenceMinMs, latenceMaxMs, tauxEchec, delaiAttenteMs);
    }

    @Override
    public ResultatProvisionnement appliquer(IdentiteReseau abonne, ConfigurationSlice configuration) {
        long latence = latenceMinMs + (latenceMaxMs > latenceMinMs ? hasard.nextLong(latenceMaxMs - latenceMinMs + 1) : 0);
        log.info("[adapter.out] NetworkProvisioningPort.appliquer imsi={} snssai={} dnn={} latence={} ms",
                abonne, configuration.snssai(), configuration.dnn(), latence);   // imsi masqué par toString()

        if (latence > delaiAttenteMs) {
            attendre(delaiAttenteMs);
            log.warn("[adapter.out] réseau simulé : délai d'attente de {} ms dépassé", delaiAttenteMs);
            return ResultatProvisionnement.indetermine("Délai d'attente du réseau dépassé (" + delaiAttenteMs + " ms)");
        }
        attendre(latence);
        if (hasard.nextDouble() < tauxEchec) {
            log.warn("[adapter.out] réseau simulé : échec de la configuration snssai={}", configuration.snssai());
            return ResultatProvisionnement.echec("Échec simulé par le réseau (MOCK_TAUX_ECHEC=" + tauxEchec + ")");
        }
        log.info("[adapter.out] réseau simulé : configuration appliquée snssai={}", configuration.snssai());
        return ResultatProvisionnement.succes();
    }

    private static void attendre(long millisecondes) {
        try {
            Thread.sleep(millisecondes);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
