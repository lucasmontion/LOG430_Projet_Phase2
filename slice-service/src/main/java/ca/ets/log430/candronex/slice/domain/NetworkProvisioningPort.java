package ca.ets.log430.candronex.slice.domain;

/**
 * Port de sortie vers le réseau 5G (UC-06, étape 3) : appliquer une configuration de slice au profil d'un abonné.
 * Nommé selon le besoin du domaine, pas selon la technologie. Phase 2 : MockOpen5GSAdapter ;
 * Phase 3 : un adaptateur Open5GS le remplacera sans aucune modification du domaine.
 *
 * Contrat : l'implémentation ne lève pas d'exception pour un problème réseau ; elle le traduit en
 * ResultatProvisionnement (ECHEC si la configuration n'a certainement pas été appliquée, INDETERMINE sinon).
 */
public interface NetworkProvisioningPort {
    ResultatProvisionnement appliquer(IdentiteReseau abonne, ConfigurationSlice configuration);
}
