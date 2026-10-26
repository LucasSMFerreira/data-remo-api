// js/ranking.js

async function carregarRanking() {
  const msg = document.getElementById('mensagem-ranking');
  const container = document.getElementById('ranking-container');

  try {
    const response = await fetch(`${API}/api/atletas/ranking`);
    if (!response.ok) throw new Error('Falha ao buscar ranking');

    const atletas = await response.json();

    if (!atletas.length) {
      msg.textContent = 'Ainda não há atletas com pontuação no ranking.';
      return;
    }

    msg.textContent = '';
    container.innerHTML = '';

    atletas.forEach((a, index) => {
      const div = document.createElement('div');
      div.textContent = `${index + 1}. ${a.nome} – ${a.pontosTotais ?? 0} pts – ${a.nivel}`;
      container.appendChild(div);
    });
  } catch (e) {
    console.error(e);
    msg.textContent = 'Erro ao carregar ranking.';
  }
}

document.addEventListener('DOMContentLoaded', carregarRanking);
