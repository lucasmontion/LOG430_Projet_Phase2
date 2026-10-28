package ca.ets.log430.candronex.commun.adapter.in.web;

import ca.ets.log430.candronex.commun.domaine.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.dao.DataIntegrityViolationException;
import ca.ets.log430.candronex.commande.adapter.out.drone.DroneServiceIndisponibleException;

/**
 * Traduit les erreurs du domaine en codes HTTP (adaptateur d'entrée).
 * Le domaine ne connaît pas HTTP : seul ce composant fait la correspondance.
 */
@RestControllerAdvice
public class GestionnaireErreurs {
    private static final Logger log = LoggerFactory.getLogger(GestionnaireErreurs.class);

    @ExceptionHandler(DonneeInvalideException.class)
    ResponseEntity<ErreurResponse> donneeInvalide(DonneeInvalideException e) {
        return reponse(HttpStatus.BAD_REQUEST, e.code(), e.getMessage());
    }

    @ExceptionHandler(ConflitException.class)
    ResponseEntity<ErreurResponse> conflit(ConflitException e) {
        return reponse(HttpStatus.CONFLICT, e.code(), e.getMessage());
    }

    @ExceptionHandler(RessourceIntrouvableException.class)
    ResponseEntity<ErreurResponse> introuvable(RessourceIntrouvableException e) {
        return reponse(HttpStatus.NOT_FOUND, e.code(), e.getMessage());
    }

    @ExceptionHandler(RegleMetierException.class)
    ResponseEntity<ErreurResponse> regleMetier(RegleMetierException e) {
        return reponse(HttpStatus.UNPROCESSABLE_ENTITY, e.code(), e.getMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
    ResponseEntity<ErreurResponse> requeteInvalide(Exception e) {
        return reponse(HttpStatus.BAD_REQUEST, "REQUETE_INVALIDE", "Requête mal formée ou champ obligatoire manquant");
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    ResponseEntity<ErreurResponse> clientNonIdentifie(MissingRequestHeaderException e) {
        return reponse(HttpStatus.UNAUTHORIZED, "CLIENT_NON_IDENTIFIE", "En-tête " + e.getHeaderName() + " manquant");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ErreurResponse> violationIntegrite(DataIntegrityViolationException e) {
        // Doublon détecté par la base (ex. deux requêtes simultanées)
        return reponse(HttpStatus.CONFLICT, "CONFLIT_DONNEES", "La ressource entre en conflit avec une ressource existante");
    }

    @ExceptionHandler(DroneServiceIndisponibleException.class)
    ResponseEntity<ErreurResponse> dependanceIndisponible(DroneServiceIndisponibleException e) {
        return reponse(HttpStatus.SERVICE_UNAVAILABLE, "DRONE_SERVICE_INDISPONIBLE", e.getMessage());
    }

    private ResponseEntity<ErreurResponse> reponse(HttpStatus statut, String code, String message) {
        log.info("[adapter.in] erreur -> {} code={}", statut.value(), code);
        return ResponseEntity.status(statut).body(new ErreurResponse(code, message));
    }
}
