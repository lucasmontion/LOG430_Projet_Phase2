package ca.ets.log430.candronex.commun.adapter.in.web;

import ca.ets.log430.candronex.commun.domaine.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.net.URI;

/**
 * Traduit les erreurs en réponses HTTP (adaptateur d'entrée), au format Problem Details
 * (RFC 9457, application/problem+json). Le domaine ne connaît pas HTTP : seul ce composant fait la correspondance.
 * Extensions au format standard : code (stable, pour les programmes clients)
 * et correlationId (pour retrouver la requête dans les journaux).
 */
@RestControllerAdvice
public class GestionnaireErreurs {
    private static final Logger log = LoggerFactory.getLogger(GestionnaireErreurs.class);

    @ExceptionHandler(DonneeInvalideException.class)
    ResponseEntity<ProblemDetail> donneeInvalide(DonneeInvalideException e, HttpServletRequest requete) {
        return reponse(HttpStatus.BAD_REQUEST, e.code(), e.getMessage(), requete);
    }

    @ExceptionHandler(ConflitException.class)
    ResponseEntity<ProblemDetail> conflit(ConflitException e, HttpServletRequest requete) {
        return reponse(HttpStatus.CONFLICT, e.code(), e.getMessage(), requete);
    }

    @ExceptionHandler(RessourceIntrouvableException.class)
    ResponseEntity<ProblemDetail> introuvable(RessourceIntrouvableException e, HttpServletRequest requete) {
        return reponse(HttpStatus.NOT_FOUND, e.code(), e.getMessage(), requete);
    }

    @ExceptionHandler(RegleMetierException.class)
    ResponseEntity<ProblemDetail> regleMetier(RegleMetierException e, HttpServletRequest requete) {
        return reponse(HttpStatus.UNPROCESSABLE_ENTITY, e.code(), e.getMessage(), requete);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class,
            MissingRequestHeaderException.class, MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class})
    ResponseEntity<ProblemDetail> requeteInvalide(Exception e, HttpServletRequest requete) {
        return reponse(HttpStatus.BAD_REQUEST, "REQUETE_INVALIDE",
                "Requête mal formée, ou champ, paramètre ou en-tête obligatoire manquant ou invalide", requete);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ProblemDetail> violationIntegrite(DataIntegrityViolationException e, HttpServletRequest requete) {
        // Doublon détecté par la base (ex. deux requêtes simultanées)
        return reponse(HttpStatus.CONFLICT, "CONFLIT_DONNEES",
                "La ressource entre en conflit avec une ressource existante", requete);
    }

    /**
     * Tout le reste. Une erreur propre à Spring MVC (route inconnue, méthode non permise...) garde son statut ;
     * toute autre est une erreur interne : 500, sans aucun détail technique dans la réponse (seulement dans le journal).
     */
    @ExceptionHandler(Exception.class)
    ResponseEntity<ProblemDetail> inattendue(Exception e, HttpServletRequest requete) {
        if (e instanceof ErrorResponse erreurSpring) {
            return reponse(HttpStatus.valueOf(erreurSpring.getStatusCode().value()), "REQUETE_NON_SUPPORTEE",
                    erreurSpring.getBody().getDetail(), requete);
        }
        log.error("[adapter.in] erreur inattendue {} {}", requete.getMethod(), requete.getRequestURI(), e);
        return reponse(HttpStatus.INTERNAL_SERVER_ERROR, "ERREUR_INTERNE",
                "Erreur interne. Communiquez le correlationId au support.", requete);
    }

    private ResponseEntity<ProblemDetail> reponse(HttpStatus statut, String code, String detail,
                                                  HttpServletRequest requete) {
        log.info("[adapter.in] erreur -> {} code={}", statut.value(), code);
        return ResponseEntity.status(statut).body(probleme(statut, code, detail, requete.getRequestURI()));
    }

    /** Construit un Problem Details ; aussi utilisé par RefusNonAuthentifie pour les 401 de Spring Security. */
    static ProblemDetail probleme(HttpStatus statut, String code, String detail, String chemin) {
        ProblemDetail probleme = ProblemDetail.forStatusAndDetail(statut, detail);
        probleme.setType(URI.create("urn:candronex:erreur:" + code));
        probleme.setInstance(URI.create(chemin));
        probleme.setProperty("code", code);
        probleme.setProperty("correlationId", MDC.get(CorrelationIdFilter.CLE_MDC));
        return probleme;
    }
}
