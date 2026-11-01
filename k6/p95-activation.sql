-- Délai d'activation de bout en bout (énoncé §6.2) : activated_at - accepted_at, base slice.
-- Fenêtre de test = services des drones créés par le run k6 (drone_id LIKE 'K6-<run>-%', run affiché par k6).
-- Un service qui n'est pas ACTIF à la fin du test compte comme dépassant 5 s (délai infini), il n'est pas exclu.
-- Lancement : voir k6/README.md (psql -v run=1234).

-- 1) P95, P50, max et nombre de services
WITH delais AS (
    SELECT CASE WHEN etat = 'ACTIF' THEN EXTRACT(EPOCH FROM activated_at - accepted_at)
                ELSE 'Infinity'::float8 END AS secondes,
           etat
    FROM service
    WHERE drone_id LIKE 'K6-' || :'run' || '-%'
)
SELECT count(*)                                                 AS services,
       count(*) FILTER (WHERE etat <> 'ACTIF')                  AS non_actifs,
       round(percentile_disc(0.50) WITHIN GROUP (ORDER BY secondes)::numeric, 3) AS p50_s,
       round(percentile_disc(0.95) WITHIN GROUP (ORDER BY secondes)::numeric, 3) AS p95_s,
       round(max(secondes) FILTER (WHERE etat = 'ACTIF')::numeric, 3)            AS max_actif_s,
       count(*) FILTER (WHERE secondes > 5)                     AS au_dela_de_5s
FROM delais;

-- 2) Services en attente d'activation, toutes les 10 s du test : ce nombre ne doit pas croître sans cesse.
--    En attente à l'instant t = accepté avant t et pas encore ACTIF à t.
WITH s AS (
    SELECT accepted_at, CASE WHEN etat = 'ACTIF' THEN activated_at END AS fin
    FROM service
    WHERE drone_id LIKE 'K6-' || :'run' || '-%'
), instants AS (
    SELECT generate_series(date_trunc('second', min(accepted_at)), max(accepted_at), interval '10 seconds') AS t
    FROM s
)
SELECT to_char(i.t, 'HH24:MI:SS') AS instant,
       count(s.*) FILTER (WHERE s.accepted_at <= i.t AND (s.fin IS NULL OR s.fin > i.t)) AS en_attente
FROM instants i CROSS JOIN s
GROUP BY i.t
ORDER BY i.t;
