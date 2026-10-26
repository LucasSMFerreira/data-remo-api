const select = document.getElementById('treino');
const statusEl = document.getElementById('status');
const sendButton = document.getElementById('simulate');
let sending = false;
let refreshing = false;
function message(text, error = false) { statusEl.textContent = text; statusEl.className = error ? 'error' : ''; }
async function api(path, body) {
  const response = await fetch(path, body === undefined ? {} : {method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(body)});
  if (!response.ok) throw new Error(`A API respondeu ${response.status}. Verifique a conexão e os dados do treino.`);
  return response.json();
}
function clear() {
  document.getElementById('rows').replaceChildren();
  document.getElementById('speed').textContent = '—'; document.getElementById('elapsed').textContent = '—';
  document.getElementById('count').textContent = '0'; document.getElementById('device').textContent = 'Nenhuma medição recebida.';
}
async function refresh() {
  if (!select.value || refreshing) return;
  refreshing = true;
  const treinoId = select.value;
  try {
    const data = await api(`/api/telemetria/treino/${treinoId}`);
    if (select.value !== treinoId) return;
    clear();
    const rows = document.getElementById('rows');
    for (const m of data) {
      const row = document.createElement('tr');
      const cells = [new Date(m.recebidoEm).toLocaleTimeString('pt-BR'), (m.tempoDecorridoMs/1000).toFixed(1),m.velocidadeKmh.toFixed(1),m.latitude.toFixed(6),m.longitude.toFixed(6),[m.aceleracaoX,m.aceleracaoY,m.aceleracaoZ].map(v=>v.toFixed(2)).join(' / '),m.origem];
      for (const value of cells) { const cell = document.createElement('td'); cell.textContent = value; row.appendChild(cell); }
      rows.appendChild(row);
    }
    document.getElementById('count').textContent = data.length;
    if (data.length) {
      const m = data[0];
      document.getElementById('speed').textContent = m.velocidadeKmh.toFixed(1);
      document.getElementById('elapsed').textContent = (m.tempoDecorridoMs/1000).toFixed(1);
      document.getElementById('device').textContent = `Dispositivo: ${m.dispositivoId} · Origem: ${m.origem} · Coordenadas fictícias`;
    }
    message(data.length ? 'Dados consultados no PostgreSQL pela API.' : 'Nenhum dado recebido. Envie a primeira simulação.');
  } catch (e) { message(e.message,true); }
  finally { refreshing = false; }
}
sendButton.addEventListener('click', async () => {
  if (sending || !select.value) return;
  sending = true; sendButton.disabled = true; select.disabled = true;
  const batch = crypto.randomUUID();
  try {
    message('Enviando 5 medições fictícias para a API...');
    for(let i=0;i<5;i++) await api('/api/telemetria',{
      eventoId:`web-${batch}-${i}`, dispositivoId:'ESP32-DEMO-WEB', treinoId:Number(select.value),
      tempoDecorridoMs:i*5000, latitude:-23.5505+i*0.0001,longitude:-46.6333+i*0.0001,
      velocidadeKmh:7+i*0.5,aceleracaoX:0.1+i*0.02,aceleracaoY:0.05,aceleracaoZ:9.81,simulacao:true
    });
    await refresh();
  } catch(e) { message(e.message+' As medições já confirmadas foram mantidas.',true); }
  finally { sending = false; sendButton.disabled = !select.value; select.disabled = false; }
});
document.getElementById('refresh').addEventListener('click',refresh);
select.addEventListener('change',()=>{clear();refresh();});
(async()=>{
  try {
    const atleta = JSON.parse(localStorage.getItem('atletaLogado') || 'null');
    if (!atleta?.id) { message('Entre na aplicação para escolher seus treinos.',true); select.replaceChildren(); sendButton.disabled=true; return; }
    const treinos = await api(`/api/treinos/atleta/${atleta.id}`);
    select.replaceChildren();
    for(const t of treinos) { const o=document.createElement('option');o.value=t.id;o.textContent=`#${t.id} · ${t.data.split('-').reverse().join('/')} · ${t.duracaoMinutos} min · ${t.distanciaKm} km`;select.appendChild(o); }
    sendButton.disabled=!treinos.length;
    if (!treinos.length) { message('Registre um treino no painel antes de enviar medições.'); return; }
    await refresh();
    setInterval(()=>{if(!document.hidden && !sending) refresh();},5000);
  } catch(e) { message(e.message,true);sendButton.disabled=true; }
})();
