package ca.ets.log430.candronex.slice.adapter.in.arriereplan;

import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.concurrent.DelegatingSecurityContextExecutor;

import java.time.Clock;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Exécution des activations en arrière-plan, à l'intérieur de slice-service (sans courtier de messages).
 * - Fils virtuels (Java 21) : une activation passe l'essentiel de son temps à attendre le réseau ;
 *   un fil virtuel par activation évite qu'une file d'attente se forme derrière un petit pool.
 * - Le contexte de sécurité (JWT du client) est propagé : l'adaptateur peut le transmettre à drone-service.
 * - Le MDC (identifiant de corrélation) est propagé : les journaux de l'activation restent reliés à la requête.
 */
@Configuration
public class ExecutionArrierePlanConfig {

    @Bean
    Executor executeurActivation() {
        Executor filsVirtuels = Executors.newVirtualThreadPerTaskExecutor();
        Executor avecMdc = tache -> {
            Map<String, String> mdc = MDC.getCopyOfContextMap();
            filsVirtuels.execute(() -> {
                if (mdc != null) MDC.setContextMap(mdc);
                try {
                    tache.run();
                } finally {
                    MDC.clear();
                }
            });
        };
        return new DelegatingSecurityContextExecutor(avecMdc);
    }

    /** Horloge injectée : le domaine et l'application reçoivent l'heure, ce qui les rend testables. */
    @Bean
    Clock horloge() {
        return Clock.systemUTC();
    }
}
