"""Integração HTTP de telemetria: cria um treino fictício e testa o PostgreSQL pela API."""
import json, uuid
from urllib.request import Request, urlopen
from urllib.error import HTTPError
base='http://127.0.0.1:8080'
checks=[]
def req(path,body=None,expected=200):
    r=Request(base+path,data=json.dumps(body).encode() if body is not None else None,headers={'Content-Type':'application/json'})
    try:
        with urlopen(r,timeout=15) as response: status,raw=response.status,response.read().decode()
    except HTTPError as e: status,raw=e.code,e.read().decode()
    assert status==expected,(path,status,expected,raw)
    return json.loads(raw)
def check(label,condition=True):
    assert condition,label
    checks.append(label);print('PASS:',label)
uid=uuid.uuid4().hex
ath=req('/api/atletas',dict(nome='Telemetria '+uid[:8],email=uid+'@example.invalid',senha='Demo2026!',modalidade='Remo'))
train=req('/api/treinos',dict(atletaId=ath['id'],data='2026-10-26',duracaoMinutos=30,distanciaKm=5,intensidade='MODERADA',tipo='BASE'))
prefix='/api/telemetria/treino/'+str(train['id'])
check('Lista inicialmente vazia',req(prefix)==[])
b=dict(eventoId='test-'+uid,dispositivoId='ESP32-TESTE',treinoId=train['id'],tempoDecorridoMs=5000,latitude=-23.5505,longitude=-46.6333,velocidadeKmh=8,aceleracaoX=.1,aceleracaoY=.05,aceleracaoZ=9.81,simulacao=True)
a=req('/api/telemetria',b)
check('Recebe e identifica simulacao',a['origem']=='SIMULACAO' and a['treinoId']==train['id'])
stored=req('/api/telemetria/evento/'+b['eventoId'])
check('Valores preservados no banco',all(stored[k]==v for k,v in b.items() if k!='simulacao') and stored['origem']=='SIMULACAO')
req('/api/telemetria/evento/inexistente',expected=404);check('Evento inexistente retorna 404')
check('Reenvio idempotente',req('/api/telemetria',b)==a and len(req(prefix))==1)
req('/api/telemetria',dict(b,velocidadeKmh=9),409);check('Conflito de evento recusado')
for field,value in [('latitude',91),('longitude',181),('velocidadeKmh',-1),('tempoDecorridoMs',-1),('dispositivoId',''),('simulacao',False),('aceleracaoX',None),('treinoId',None)]:
    req('/api/telemetria',dict(b,**{field:value}),400);check('Recusa campo invalido '+field)
req('/api/telemetria',dict(b,eventoId='missing-'+uid,treinoId=9223372036854775807),404);check('Recusa treino inexistente')
req('/api/telemetria',{},400);check('Recusa pacote incompleto')
check('Falhas nao gravam medicoes',len(req(prefix))==1)
# Outro treino não pode herdar medições.
t2=req('/api/treinos',dict(atletaId=ath['id'],data='2026-10-26',duracaoMinutos=10,distanciaKm=1,intensidade='LEVE',tipo='BASE'))
check('Isolamento por treino',req('/api/telemetria/treino/'+str(t2['id']))==[])
before=req('/api/atletas/'+str(ath['id']))
for i in range(1,55):req('/api/telemetria',dict(b,eventoId='test-'+uid+'-'+str(i),tempoDecorridoMs=i*5000))
rows=req(prefix)
check('Limite de 50 medicoes recentes',len(rows)==50)
check('Ordenacao por recebimento',all(rows[i]['recebidoEm']>=rows[i+1]['recebidoEm'] for i in range(len(rows)-1)))
check('Mede sem alterar pontos ou treinos',req('/api/atletas/'+str(ath['id']))==before)
print(json.dumps(dict(resultado='APROVADO',verificacoes=len(checks),treinoId=train['id']),ensure_ascii=False))
