package ca.ets.log430.candronex.commun.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 401 au format Problem Details, comme toutes les autres erreurs.
 * Spring Security refuse la requête AVANT les contrôleurs : GestionnaireErreurs ne la voit jamais, d'où ce composant.
 * L'en-tête standard WWW-Authenticate: Bearer (RFC 6750) est conservé.
 */
@Component
public class RefusNonAuthentifie implements AuthenticationEntryPoint {
    private static final Logger log = LoggerFactory.getLogger(RefusNonAuthentifie.class);

    private final BearerTokenAuthenticationEntryPoint standard = new BearerTokenAuthenticationEntryPoint();
    private final ObjectMapper json;

    public RefusNonAuthentifie(ObjectMapper json) {
        this.json = json;
    }

    @Override
    public void commence(HttpServletRequest requete, HttpServletResponse reponse,
                         AuthenticationException e) throws IOException {
        log.info("[adapter.in] {} {} -> 401 : {}", requete.getMethod(), requete.getRequestURI(), e.getMessage());
        standard.commence(requete, reponse, e);   // statut 401 + en-tête WWW-Authenticate
        reponse.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        reponse.setCharacterEncoding("UTF-8");
        json.writeValue(reponse.getOutputStream(), GestionnaireErreurs.probleme(HttpStatus.UNAUTHORIZED,
                "NON_AUTHENTIFIE", "Jeton Bearer absent, invalide ou expiré", requete.getRequestURI()));
    }
}
