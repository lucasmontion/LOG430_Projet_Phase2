-- commande-service (UC-04). Base propre au service : contextes Catalogue et Gestion des commandes.
-- Aucune clé étrangère vers le drone : il appartient à drone-service et n'est référencé que par identifiant.

-- Catalogue. Les paramètres réseau sont des DONNÉES, pas du code :
-- ajouter ou modifier un service ne demande aucun changement de classe.
CREATE TABLE specification_service (
    type_service    VARCHAR(20)  NOT NULL,
    nom             VARCHAR(100) NOT NULL,
    sst             INTEGER      NOT NULL,
    sd              VARCHAR(6)   NOT NULL,
    dnn             VARCHAR(64)  NOT NULL,
    five_qi         INTEGER      NOT NULL,
    arp             INTEGER      NOT NULL,
    ambr_montant    VARCHAR(20)  NOT NULL,
    ambr_descendant VARCHAR(20)  NOT NULL,
    CONSTRAINT pk_specification_service PRIMARY KEY (type_service),
    CONSTRAINT ck_spec_sst CHECK (sst BETWEEN 0 AND 255),
    CONSTRAINT ck_spec_arp CHECK (arp BETWEEN 1 AND 15)
);

-- Données de démonstration : valeurs de la commande de référence (dossier de domaine §7.2)
INSERT INTO specification_service
    (type_service, nom, sst, sd, dnn, five_qi, arp, ambr_montant, ambr_descendant)
VALUES
    ('C2',       'C&C Connectivity',     2, '000001', 'c2',      7, 2, '20 Mbps',  '20 Mbps'),
    ('IMAGERIE', 'Imagery Connectivity', 1, '000002', 'imagery', 9, 8, '500 Mbps', '100 Mbps');

-- Gestion des commandes
CREATE TABLE commande (
    id              UUID         NOT NULL,
    client_id       VARCHAR(64)  NOT NULL,
    cle_idempotence VARCHAR(100) NOT NULL,
    cree_le         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_commande PRIMARY KEY (id),
    CONSTRAINT uq_commande_idempotence UNIQUE (client_id, cle_idempotence)  -- rejeu impossible en double
);

CREATE TABLE element_commande (
    id              UUID        NOT NULL,
    commande_id     UUID        NOT NULL,
    rang            INTEGER     NOT NULL,
    drone_id        VARCHAR(64) NOT NULL,
    type_service    VARCHAR(20) NOT NULL,
    etat            VARCHAR(20) NOT NULL,
    -- caractéristiques COPIÉES du catalogue à la soumission (figées)
    sst             INTEGER     NOT NULL,
    sd              VARCHAR(6)  NOT NULL,
    dnn             VARCHAR(64) NOT NULL,
    five_qi         INTEGER     NOT NULL,
    arp             INTEGER     NOT NULL,
    ambr_montant    VARCHAR(20) NOT NULL,
    ambr_descendant VARCHAR(20) NOT NULL,
    CONSTRAINT pk_element_commande PRIMARY KEY (id),
    CONSTRAINT fk_element_commande FOREIGN KEY (commande_id) REFERENCES commande (id),
    CONSTRAINT uq_element_drone_service UNIQUE (commande_id, drone_id, type_service),
    CONSTRAINT ck_element_etat CHECK (etat IN ('RECU', 'EN_COURS', 'ACTIF', 'ECHEC', 'ANNULE', 'COMPENSE'))
);
