# Test de charge (k6)

- `charge.js` : 1 min de montée (`ramping-arrival-rate`) jusqu'à 600 opérations/s, puis 5 min à 600 opérations/s
  (`constant-arrival-rate`), vers la passerelle. Mélange : 10 % enregistrements de drones, 30 % commandes
  (C2 + Imagerie), 60 % consultations (30 % drone, 30 % commande).
- `p95-activation.sql` : P95 de `activated_at - accepted_at` (un service non actif compte comme dépassant 5 s),
  et nombre de services en attente toutes les 10 s.
- `resultats/resume.json` : export de `handleSummary()`.

La pile doit tourner (`docker compose up -d`). k6 tourne dans un conteneur, sur le réseau `public` du compose
(la passerelle est jointe par `api-gateway:8080`, Keycloak par `keycloak:8080`). Les secrets des clients
de démonstration sont lus dans `.env`.

```bash
MSYS_NO_PATHCONV=1 docker run --rm --network candronex_public --env-file .env -v "$(pwd -W)/k6:/scripts" grafana/k6:0.54.0 run /scripts/charge.js
```

(Git Bash ; en PowerShell, retirer `MSYS_NO_PATHCONV=1` et remplacer `$(pwd -W)` par `${PWD}`.) Essai court : ajouter `-e DEBIT=20 -e PALIER=20s`.

k6 affiche au début `run 1234 : ...`. Calculer ensuite le P95 sur ce run :

```bash
docker compose exec -T slice-db psql -U slice -d slice -v run=1234 < k6/p95-activation.sql
```

Seuils vérifiés par k6 : aucune itération abandonnée (`dropped_iterations`), moins de 1 % d'erreurs
(`http_req_failed`).

Limite : l'API publique ne donne pas le `serviceId` d'un élément de commande ; la consultation directe
de l'état d'un service n'est donc pas dans le mélange. La consultation d'une commande lit cependant
l'état de ses services dans slice-service.
