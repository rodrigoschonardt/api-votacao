import http from 'k6/http';
import { check, sleep } from 'k6';
import { randomString, randomIntBetween } from 'https://jslib.k6.io/k6-utils/1.2.0/index.js';

const BASE_URL = 'http://voting-api:8080/api/v1';

const headers = {
    'Content-Type': 'application/json',
};

export const options = {
    vus: 1000, // "usuários" ativos
    iterations: 1000, // uma iteração para cada
    thresholds: {
        http_req_duration: ['p(95)<2000'], // “95% das requisições devem ter duração inferior a 2 segundos”
        http_req_failed: ['rate<0.01'], // Menos de 1% das requisições devem falhar
    },
};

// Função para gerar CPF válido
function generateCPF() {
    const digits = [];
    for (let i = 0; i < 9; i++) {
        digits.push(randomIntBetween(0, 9));
    }

    const d1 = digits.reduce((acc, digit, index) => acc + digit * (10 - index), 0) % 11;
    const d2 = (digits.concat(d1 < 2 ? 0 : 11 - d1)).reduce((acc, digit, index) => acc + digit * (11 - index), 0) % 11;

    digits.push(d1 < 2 ? 0 : 11 - d1);
    digits.push(d2 < 2 ? 0 : 11 - d2);

    return digits.join('').replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4');
}

export function setup() {
    console.log('Setup iniciado');

    // Criação da pauta
    const topicData = {
        title: 'Pauta simulação',
        description: `Simulação de votação`,
    };

    // Criação da pauta
    const topicRes = http.post(`${BASE_URL}/topics`, JSON.stringify(topicData), { headers });
    if (topicRes.status !== 201) throw new Error('Erro ao criar tópico');
    const topicId = JSON.parse(topicRes.body).id;

    // Criação da sessão
    const sessionRes = http.post(`${BASE_URL}/sessions`, JSON.stringify({ topicId: topicId, duration: 10 }), { headers });
    if (sessionRes.status !== 201) throw new Error('Erro ao criar sessão');
    const sessionId = JSON.parse(sessionRes.body).id;

    // Criação dos usuários
    const users = [];
    for (let i = 0; i < 1000; i++) {
        const cpf = generateCPF();
        const userRes = http.post(`${BASE_URL}/users`, JSON.stringify({ cpf }), { headers });
        if (userRes.status === 201) {
            users.push(JSON.parse(userRes.body).id);
        } else {
            console.warn(`Erro ao criar usuário ${i + 1}: ${userRes.status}`);
        }
    }

    console.log('Setup concluído');
    return { topicId, sessionId, users };
}

export default function (data) {
    const { sessionId, users } = data;

    const userIndex = __VU - 1;
    if (userIndex >= users.length) {
        console.warn(`Usuário não disponível para VU ${__VU}`);
        return;
    }

    // Atraso aleatório entre 0.1 e 10 segundos para simular comportamento humano
    sleep(randomFloatBetween(0.1, 10.0));

    const userId = users[userIndex];
    const votos = ['Sim', 'Não'];
    const voto = votos[randomIntBetween(0, 1)];

    const votoData = {
        voteOption: voto,
        userId: userId,
        sessionId: sessionId,
    };

    const res = http.post(`${BASE_URL}/votes`, JSON.stringify(votoData), { headers });

    check(res, {
        'Voto registrado': (r) => r.status === 201,
    });
}

export function teardown(data) {
    console.log('Consultando resultado final...');
    const res = http.get(`${BASE_URL}/topics/result/${data.topicId}`);
    if (res.status === 200) {
        console.log(`Resultado: ${res.body}`);
    } else {
        console.error(`Erro ao consultar resultado: ${res.status} - ${res.body}`);
    }
}

function randomFloatBetween(min, max) {
    return Math.random() * (max - min) + min;
}