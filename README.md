# CanDroneX — Phase 2 (microservices)

Trois microservices Spring Boot (Java 21), chacun avec sa base PostgreSQL, derrière une passerelle KrakenD.
Les jetons sont émis par Keycloak (OAuth2 client credentials).

| Conteneur | Rôle | Cas d'utilisation | Exposé sur l'hôte |
|---|---|---|---|
| `api-gateway` (KrakenD) | Point d'entrée unique, vérifie le jeton | — | `GATEWAY_PORT` (8088 dans `.env.example`) |
| `keycloak` | Émet les jetons (realm `candronex`) | — | 8081 |
| `drone-service` + `drone-db` | Gestion des drones | UC-02 | non |
| `commande-service` + `commande-db` | Catalogue et commandes | UC-04 | non |
| `slice-service` + `slice-db` | Activation et inventaire des services | UC-06 | non |
| `swagger-ui` | Contrats OpenAPI | — | 8090 |

Réseaux : `public` relie la passerelle, Keycloak et les 3 services ; chaque service a un réseau privé
avec sa base (`donnees-drone`, `donnees-commande`, `donnees-slice`), qu'aucun autre conteneur ne peut joindre.

## Démarrer

```bash
cp .env.example .env      # puis renseigner les secrets ; ne jamais versionner .env
docker compose up -d --build
```

Réinitialiser les bases : `docker compose down -v`.

Le réseau 5G est simulé par le `MockOpen5GSAdapter` de slice-service. Ses paramètres se règlent dans `.env` :
`MOCK_LATENCE_MIN_MS` / `MOCK_LATENCE_MAX_MS` (300 à 1500 ms par défaut), `MOCK_TAUX_ECHEC` (0.0 par défaut)
et `MOCK_DELAI_ATTENTE_MS` (5000 ms).

## Obtenir un jeton

Clients B2B de démonstration : `inspectra-demo` et `aerotech-demo` (secrets dans `.env`).

```bash
TOKEN=$(curl -s http://localhost:8081/realms/candronex/protocol/openid-connect/token \
  -d grant_type=client_credentials -d client_id=inspectra-demo \
  --data-urlencode "client_secret=$INSPECTRA_DEMO_SECRET" | jq -r .access_token)
```

## Routes publiques (par la passerelle)

| Méthode | Route | |
|---|---|---|
| POST | `/api/drone-service/v1/drone` | UC-02 : enregistrer un drone |
| GET | `/api/drone-service/v1/drone/{droneId}` | UC-02 : consulter un drone |
| POST | `/api/commande-service/v1/commande` | UC-04 : commander (en-tête `Idempotency-Key` obligatoire) |
| GET | `/api/commande-service/v1/commande/{commandeId}` | UC-04 : consulter une commande et l'état de ses éléments |
| GET | `/api/slice-service/v1/service/{serviceId}` | UC-06 : consulter l'état d'un service |

Les routes entre services (identité réseau, activation, services d'une commande) ne sont pas exposées
par la passerelle. Les erreurs suivent le format Problem Details (RFC 9457).

## Scénario de démonstration

```bash
GW=http://localhost:8088   # port choisi dans GATEWAY_PORT

curl -i -X POST $GW/api/drone-service/v1/drone \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"droneId":"DRONE-600","imsi":"999700000000001","iccid":"8999970000000000101","typeSim":"ESIM"}'

curl -i -X POST $GW/api/commande-service/v1/commande \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -H 'Idempotency-Key: demo-600' \
  -d '{"elements":[{"droneId":"DRONE-600","typeService":"C2"},{"droneId":"DRONE-600","typeService":"IMAGERIE"}]}'

# Quelques secondes plus tard : commande COMPLETEE, les deux éléments ACTIF
curl -s $GW/api/commande-service/v1/commande/<commandeId> -H "Authorization: Bearer $TOKEN"
```

Les journaux des 3 services portent le `correlationId` et le `commandeId` : `docker compose logs -f`.

## Contrats OpenAPI

Swagger UI : http://localhost:8090. Les contrats sont dans `openapi/` : `api-gateway.yaml` (API publique,
écrit à la main) et un fichier par service, régénéré à partir du code à chaque `mvn test`.

## Tests

```bash
mvn test        # H2 en mémoire, aucune base ni Keycloak requis
```

Test de charge et P95 du délai d'activation : voir [k6/README.md](k6/README.md).

## Structure

```
pom.xml                    POM parent (Maven multi-modules)
drone-service/  commande-service/  slice-service/
  src/main/java/.../<service>/
    domain/                agrégats, objets valeur, règles, ports (Java pur)
    application/           services applicatifs
    adapter/in/web/        contrôleurs REST, DTO, contrat OpenAPI
    adapter/out/...        persistance JPA, appels REST vers les autres services, mock 5G
  src/main/java/.../commun/   copié dans chaque service (pas de librairie partagée)
  (commande-service contient aussi le module catalogue/)
krakend/krakend.json       routes publiques de la passerelle
keycloak/                  realm importé au démarrage
openapi/                   contrats OpenAPI
k6/                        test de charge et requête SQL du P95
```
