package ca.ets.log430.candronex.commun.adapter.in.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Traçabilité (CRTC) : attribue un identifiant de corrélation à chaque requête.
 * Il apparaît sur chaque ligne de journal de la requête et est renvoyé au client,
 * ce qui permet de reconstituer une opération de bout en bout.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)   // s'exécute avant tout le reste
public class CorrelationIdFilter extends OncePerRequestFilter {

    public static final String EN_TETE = "X-Correlation-Id";
    public static final String CLE_MDC = "correlationId";

    // Un identifiant fourni par le client n'est accepté que s'il est sûr :
    // empêche l'injection de fausses lignes dans les journaux (retours à la ligne, etc.)
    private static final Pattern FORMAT = Pattern.compile("[A-Za-z0-9-]{1,64}");

    @Override
    protected void doFilterInternal(HttpServletRequest requete, HttpServletResponse reponse,
                                    FilterChain chaine) throws ServletException, IOException {
        String recu = requete.getHeader(EN_TETE);
        String id = (recu != null && FORMAT.matcher(recu).matches()) ? recu : UUID.randomUUID().toString();

        MDC.put(CLE_MDC, id);
        reponse.setHeader(EN_TETE, id);   // posé avant le traitement : présent même sur les erreurs
        try {
            chaine.doFilter(requete, reponse);
        } finally {
            MDC.remove(CLE_MDC);          // le fil d'exécution est réutilisé pour d'autres requêtes
        }
    }
}