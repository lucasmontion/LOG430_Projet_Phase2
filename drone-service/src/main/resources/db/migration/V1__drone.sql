-- Module drone (UC-02). Base propre au service : aucun autre service n'y accède.
-- Les contraintes garantissent l'intégrité même si le code se trompe.
CREATE TABLE drone (
    client_id  VARCHAR(64) NOT NULL,
    drone_id   VARCHAR(64) NOT NULL,
    imsi       VARCHAR(15) NOT NULL,
    iccid      VARCHAR(20) NOT NULL,
    type_sim   VARCHAR(4) NOT NULL,
    statut     VARCHAR(8) NOT NULL,
    cree_le    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_drone        PRIMARY KEY (client_id, drone_id),      -- droneId unique chez un client
    CONSTRAINT uq_drone_imsi   UNIQUE (imsi),                          -- correspondance droneId <-> IMSI non ambiguë
    CONSTRAINT ck_drone_statut CHECK (statut IN ('ACTIF', 'SUSPENDU', 'RETIRE')),
    CONSTRAINT uq_drone_iccid UNIQUE (iccid),
    CONSTRAINT ck_drone_type_sim CHECK (type_sim IN ('SIM', 'ESIM'))
);
