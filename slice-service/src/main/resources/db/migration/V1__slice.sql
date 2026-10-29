-- slice-service (UC-06). Base propre au service : inventaire des services de connectivité.
-- commande_id, element_id et drone_id appartiennent à d'autres services : simples colonnes, aucune clé étrangère.
-- L'IMSI n'est jamais stocké ici : il est demandé à drone-service au moment de l'activation.
CREATE TABLE service (
    id              UUID         NOT NULL,
    commande_id     UUID         NOT NULL,
    element_id      UUID         NOT NULL,
    client_id       VARCHAR(64)  NOT NULL,
    drone_id        VARCHAR(64)  NOT NULL,
    type_service    VARCHAR(20)  NOT NULL,
    -- paramètres réseau à appliquer (S-NSSAI = sst + sd, DNN, QoS)
    sst             INTEGER      NOT NULL,
    sd              VARCHAR(6)   NOT NULL,
    dnn             VARCHAR(64)  NOT NULL,
    five_qi         INTEGER      NOT NULL,
    arp             INTEGER      NOT NULL,
    ambr_montant    VARCHAR(20)  NOT NULL,
    ambr_descendant VARCHAR(20)  NOT NULL,
    etat            VARCHAR(20)  NOT NULL,
    accepted_at     TIMESTAMP WITH TIME ZONE NOT NULL,   -- acceptation de la commande (début de la mesure du P95)
    activated_at    TIMESTAMP WITH TIME ZONE,            -- passage à ACTIF (fin de la mesure du P95)
    cause_echec     VARCHAR(255),
    CONSTRAINT pk_service PRIMARY KEY (id),
    -- Idempotence de l'activation : un seul service par élément de commande,
    -- donc jamais deux associations slice <-> drone pour une même demande logique
    CONSTRAINT uq_service_origine UNIQUE (commande_id, element_id),
    CONSTRAINT ck_service_etat CHECK (etat IN ('EN_ATTENTE', 'EN_ACTIVATION', 'ACTIF', 'ECHEC', 'ECHEC_INDETERMINE')),
    CONSTRAINT ck_service_actif_date CHECK (etat <> 'ACTIF' OR activated_at IS NOT NULL)
);

-- Lecture des services d'une commande (calcul de l'état global par commande-service)
CREATE INDEX ix_service_client_commande ON service (client_id, commande_id);
