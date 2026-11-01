// Test de charge CanDroneX (énoncé §6.2) : taux d'arrivée constant, dirigé vers la passerelle KrakenD.
// 1 min de montée jusqu'à 600 opérations/s, puis 5 min à 600 opérations/s.
// Mélange : 60 % consultations (drone, commande), 30 % prises de commande, 10 % enregistrements de drones.
// Lancement : voir k6/README.md. Les secrets des clients viennent de .env (jamais écrits ici).
import http from 'k6/http';
import exec from 'k6/execution';
import { check } from 'k6';
import { textSummary } from 'https://jslib.k6.io/k6-summary/0.1.0/index.js';
import { uuidv4 } from 'https://jslib.k6.io/k6-utils/1.4.0/index.js';

const PASSERELLE = __ENV.BASE_URL || 'http://api-gateway:8080';
const KEYCLOAK = __ENV.KEYCLOAK_URL || 'http://keycloak:8080';
const DEBIT = Number(__ENV.DEBIT || 600);             // opérations/s pendant le palier
const PALIER = __ENV.PALIER || '5m';
const CLIENTS = [
    { id: 'inspectra-demo', secret: __ENV.INSPECTRA_DEMO_SECRET },
    { id: 'aerotech-demo', secret: __ENV.AEROTECH_DEMO_SECRET },
];

export const options = {
    scenarios: {
        montee: {
            executor: 'ramping-arrival-rate',
            startRate: 0, timeUnit: '1s',
            stages: [{ target: DEBIT, duration: '1m' }],
            preAllocatedVUs: 200, maxVUs: 1500,
        },
        palier: {
            executor: 'constant-arrival-rate',
            rate: DEBIT, timeUnit: '1s', duration: PALIER, startTime: '1m',
            preAllocatedVUs: 400, maxVUs: 1500,
        },
    },
    // Critères de l'énoncé : aucune itération abandonnée, moins de 1 % d'erreurs
    thresholds: {
        dropped_iterations: ['count==0'],
        http_req_failed: ['rate<0.01'],
        'http_req_duration{scenario:palier}': ['p(95)<5000'],
    },
    setupTimeout: '2m',
};

// ---- Jeton : un par client et par VU, renouvelé avant son expiration (300 s dans le realm) ----
const jetons = {};
function jeton(client) {
    const j = jetons[client.id];
    if (j && Date.now() < j.expire) return j.valeur;
    const r = http.post(`${KEYCLOAK}/realms/candronex/protocol/openid-connect/token`,
        { grant_type: 'client_credentials', client_id: client.id, client_secret: client.secret },
        { tags: { name: 'jeton' } });
    if (r.status !== 200) throw new Error(`jeton refusé pour ${client.id} : ${r.status}`);
    const corps = r.json();
    jetons[client.id] = { valeur: corps.access_token, expire: Date.now() + (corps.expires_in - 60) * 1000 };
    return corps.access_token;
}

function entetes(client, extra = {}) {
    return { headers: Object.assign({ Authorization: `Bearer ${jeton(client)}`,
        'Content-Type': 'application/json' }, extra) };
}

// ---- Identifiants uniques : run (4 chiffres) + VU (4) + compteur local (4) ----
let compteur = 0;
function nouveauDrone(run, vu) {
    compteur++;
    const suffixe = `${run}${String(vu).padStart(4, '0')}${String(compteur).padStart(4, '0')}`;
    return {
        droneId: `K6-${run}-${vu}-${compteur}`,
        imsi: `999${suffixe}`,              // 15 chiffres
        iccid: `89${suffixe}00000`,         // 19 chiffres
        typeSim: 'ESIM',
    };
}

function enregistrerDrone(client, run, vu) {
    const drone = nouveauDrone(run, vu);
    const r = http.post(`${PASSERELLE}/api/drone-service/v1/drone`, JSON.stringify(drone),
        Object.assign(entetes(client), { tags: { name: 'POST drone' } }));
    check(r, { 'drone 201': (x) => x.status === 201 });
    return r.status === 201 ? drone.droneId : null;
}

function passerCommande(client, droneId) {
    // Commande de référence de l'énoncé : C2 + Imagerie pour un même drone
    const corps = JSON.stringify({ elements: [
        { droneId, typeService: 'C2' }, { droneId, typeService: 'IMAGERIE' }] });
    const r = http.post(`${PASSERELLE}/api/commande-service/v1/commande`, corps,
        Object.assign(entetes(client, { 'Idempotency-Key': uuidv4() }), { tags: { name: 'POST commande' } }));
    check(r, { 'commande 201': (x) => x.status === 201 });
    return r.status === 201 ? r.json('commandeId') : null;
}

// ---- Préparation : quelques drones et commandes par client, pour les consultations ----
export function setup() {
    const run = String(Math.floor(Date.now() / 1000) % 10000).padStart(4, '0');
    const donnees = { run, pools: {} };
    for (const client of CLIENTS) {
        const drones = [];
        const commandes = [];
        for (let i = 0; i < 20; i++) {
            const droneId = enregistrerDrone(client, run, 0);
            if (droneId) drones.push(droneId);
        }
        for (const droneId of drones.slice(0, 10)) {
            const commandeId = passerCommande(client, droneId);
            if (commandeId) commandes.push(commandeId);
        }
        donnees.pools[client.id] = { drones, commandes };
    }
    console.log(`run ${run} : filtrer la base avec drone_id LIKE 'K6-${run}-%'`);
    return donnees;
}

// Ressources créées par ce VU, ajoutées aux pools de départ
const mesDrones = {};
const mesCommandes = {};
const auHasard = (t) => t[Math.floor(Math.random() * t.length)];

export default function (donnees) {
    const vu = exec.vu.idInTest;
    const client = CLIENTS[vu % CLIENTS.length];
    if (!mesDrones[client.id]) {
        mesDrones[client.id] = [...donnees.pools[client.id].drones];
        mesCommandes[client.id] = [...donnees.pools[client.id].commandes];
    }
    const drones = mesDrones[client.id];
    const commandes = mesCommandes[client.id];

    const tirage = Math.random();
    if (tirage < 0.10) {
        const droneId = enregistrerDrone(client, donnees.run, vu);
        if (droneId) drones.push(droneId);
    } else if (tirage < 0.40) {
        const commandeId = passerCommande(client, auHasard(drones));
        if (commandeId) commandes.push(commandeId);
    } else if (tirage < 0.70) {
        const r = http.get(`${PASSERELLE}/api/drone-service/v1/drone/${auHasard(drones)}`,
            Object.assign(entetes(client), { tags: { name: 'GET drone' } }));
        check(r, { 'drone 200': (x) => x.status === 200 });
    } else {
        // Consulter une commande lit aussi l'état de ses services dans slice-service
        const r = http.get(`${PASSERELLE}/api/commande-service/v1/commande/${auHasard(commandes)}`,
            Object.assign(entetes(client), { tags: { name: 'GET commande' } }));
        check(r, { 'commande 200': (x) => x.status === 200 });
    }
}

export function handleSummary(resume) {
    return {
        stdout: textSummary(resume, { indent: ' ', enableColors: true }),
        '/scripts/resultats/resume.json': JSON.stringify(resume, null, 2),
    };
}
