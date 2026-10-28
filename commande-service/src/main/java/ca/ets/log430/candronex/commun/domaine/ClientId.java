package ca.ets.log430.candronex.commun.domaine;

/**
 * Objet valeur partagé : identifiant du client B2B.
 * Seul objet métier partagé entre modules — garder le module commun minuscule.
 */
public record ClientId(String valeur) {
    public ClientId {
        if (valeur == null || valeur.isBlank()) {
            throw new DonneeInvalideException("CLIENT_ID_INVALIDE", "L'identifiant du client est obligatoire");
        }
    }
}
