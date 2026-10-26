const select = document.getElementById('treino');
const statusEl = document.getElementById('status');
const sendButton = document.getElementById('simulate');
let sending = false;
let refreshing = false;
let currentData = [];
const verification = document.getElementById('verification');
function batchKey() { return `telemetria-lote-${select.value}`; }
function lastBatch() { return JSON.parse(localStorage.getItem(batchKey()) || '[]'); }
function verificationMessage(text) { verification.textContent=text; }
function sameValues(expected, actual) {
  return Object.entries(expected).every(([key,value]) => key === 'simulacao' ? actual.origem === 'SIMULACAO' :
    typeof value === 'number' ? typeof actual[key] === 'number' && Math.abs(actual[key]-value)<1e-8 : actual[key]===value);
}
async function verifyBatch() {
  const batch=lastBatch();
  if (!batch.length) { verificationMessage('Nenhum lote enviado por este navegador para conferir.'); return; }
  let valid=0;
  for (const expected of batch) {
    const actual=await api(`/api/telemetria/evento/${encodeURIComponent(expected.eventoId)}`);
    if (!sameValues(expected,actual)) throw new Error('Os valores consultados divergem do pacote enviado.');
    valid++;
  }
  verificationMessage(`Conferência aprovada: ${valid}/${batch.length} medições consultadas na API coincidem com os valores enviados (identificadores, treino, tempo, posição, velocidade e aceleração).`);
}
async function sendBatch(batch) {
  if(sending || !select.value) return;
  sending=true;sendButton.disabled=true;select.disabled=true;
  document.getElementById('retry').disabled=true;
  try {
    for(let i=0;i<batch.length;i++) {
      message(`Enviando ${i+1}/${batch.length} medições fictícias...`);
      const actual=await api('/api/telemetria',batch[i]);
      if(!sameValues(batch[i],actual)) throw new Error('A confirmação diverge dos valores enviados.');
    }
    await refresh();
    await verifyBatch();
  } catch(e) {
    verificationMessage('Envio ou conferência não concluídos. Use Reenviar último lote para retomar sem duplicar registros.');
    message(e.message,true);
  } finally { sending=false;sendButton.disabled=!select.value;select.disabled=false;document.getElementById('retry').disabled=false; }
}
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
    currentData = data;
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
  const batchId=crypto.randomUUID();
  const batch=Array.from({length:5},(_,i)=>({
    eventoId:`web-${batchId}-${i}`,dispositivoId:'ESP32-DEMO-WEB',treinoId:Number(select.value),
    tempoDecorridoMs:i*5000,latitude:-23.5505+i*.0001,longitude:-46.6333+i*.0001,
    velocidadeKmh:7+i*.5,aceleracaoX:.1+i*.02,aceleracaoY:.05,aceleracaoZ:9.81,simulacao:true
  }));
  localStorage.setItem(batchKey(),JSON.stringify(batch));
  await sendBatch(batch);
});
document.getElementById('retry').addEventListener('click', async()=>{
  const batch=lastBatch();
  if(!batch.length) {verificationMessage('Nenhum lote deste navegador para reenviar.');return;}
  await sendBatch(batch);
});
document.getElementById('verify').addEventListener('click',async()=>{
  try {await verifyBatch();} catch(e) {verificationMessage('Conferência não concluída: '+e.message);}
});
document.getElementById('export').addEventListener('click',()=>{
  if(!currentData.length) {message('Não há medições para baixar.',true);return;}
  const link=document.createElement('a');
  const url=URL.createObjectURL(new Blob([JSON.stringify(currentData,null,2)],{type:'application/json'}));
  link.href=url;link.download=`telemetria-treino-${select.value}.json`;link.click();setTimeout(()=>URL.revokeObjectURL(url),1000);
});
document.getElementById('refresh').addEventListener('click',refresh);
select.addEventListener('change',()=>{clear();currentData=[];verificationMessage('');refresh();});
(async()=>{
  try {
    const atleta = JSON.parse(localStorage.getItem('atletaLogado') || 'null');
    if (!atleta?.id) { message('Entre na aplicação para escolher seus treinos. Use Voltar ao painel para acessar o login.',true); select.replaceChildren(); sendButton.disabled=true; return; }
    const treinos = await api(`/api/treinos/atleta/${atleta.id}`);
    select.replaceChildren();
    for(const t of treinos) { const o=document.createElement('option');o.value=t.id;o.textContent=`#${t.id} · ${t.data.split('-').reverse().join('/')} · ${t.duracaoMinutos} min · ${t.distanciaKm} km`;select.appendChild(o); }
    sendButton.disabled=!treinos.length;
    if (!treinos.length) { message('Registre um treino no painel antes de enviar medições.'); return; }
    await refresh();
    setInterval(()=>{if(!document.hidden && !sending) refresh();},5000);
  } catch(e) { message(e.message,true);sendButton.disabled=true; }
})();
