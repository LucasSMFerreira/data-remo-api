"""Teste funcional contra uma API LOCAL em execução; cria somente atletas fictícios."""
import json, sys, uuid
from urllib.request import Request, urlopen
from urllib.error import HTTPError
from urllib.parse import urlparse
base = sys.argv[1] if len(sys.argv) > 1 else 'http://127.0.0.1:8080'
assert urlparse(base).hostname in ('localhost','127.0.0.1'), 'Use somente ambiente local.'
checks=[]
def request(path, body=None, expected=200):
    req=Request(base+path,data=json.dumps(body).encode() if body is not None else None,headers={'Content-Type':'application/json'})
    try:
        with urlopen(req,timeout=15) as r: status, raw = r.status,r.read().decode()
    except HTTPError as e: status,raw=e.code,e.read().decode()
    assert status==expected,(path,status,expected,raw)
    try: return json.loads(raw)
    except ValueError: return raw

def check(name, condition):
    assert condition,name
    checks.append(name)
    print('PASS:',name)

check('API responde',request('/api/ping')=='ok')
for page in ['/', '/dashboard.html','/registrar-remada.html','/ranking.html','/perfil.html','/atletas.html','/js/vendor/chart.umd.min.js']:
    check('Recurso servido '+page,bool(request(page)))
name='Teste API '+uuid.uuid4().hex[:8]
body={'nome':name,'email':uuid.uuid4().hex+'@example.invalid','senha':'Ficticia2026!','idade':24,'modalidade':'Remo','plano':'FREE'}
a=request('/api/atletas',body)
check('Cadastro sem expor senha','senha' not in a and a['totalTreinos']==0 and a['pontosTotais']==0)
for field,value in [('email','invalido'),('idade',-1),('senha','123')]:
    request('/api/atletas',dict(body,**{field:value}),400)
    check('Cadastro invalido recusado: '+field,True)
malicious=dict(body,nome=name+' outro',email=uuid.uuid4().hex+'@example.invalid',id=a['id'],pontosTotais=99999,totalTreinos=999)
clean=request('/api/atletas',malicious)
check('Cadastro inicia com acumulados zerados',clean['id']!=a['id'] and clean['pontosTotais']==0 and clean['totalTreinos']==0)
request('/api/atletas',body,409);check('Cadastro duplicado recusado',True)
request('/api/atletas',{'nome':'Incompleto'},400);check('Cadastro incompleto recusado',True)
request('/api/atletas/login',{'nome':name,'senha':'errada'},401);check('Senha incorreta recusada',True)
check('Login correto',request('/api/atletas/login',{'nome':name,'senha':body['senha']})['id']==a['id'])
path='/api/treinos/atleta/'+str(a['id'])
check('Histórico inicialmente vazio',request(path)==[])
t={'atletaId':a['id'],'data':'2026-10-26','duracaoMinutos':30,'distanciaKm':5,'intensidade':'MODERADA','tipo':'BASE','notas':'Teste automático fictício'}
for field,value in [('duracaoMinutos',-1),('distanciaKm',0),('atletaId',None),('tipo',None),('intensidade','INVALIDA'),('notas','x'*501)]:
    request('/api/treinos',dict(t,**{field:value}),400)
    check('Treino inválido recusado: '+field,True)
request('/api/treinos',dict(t,atletaId=9223372036854775807),404)
check('Atleta inexistente retorna 404',True)
check('Treinos inválidos não alteram histórico',request(path)==[])
for i in range(5):
    saved=request('/api/treinos',dict(t,data='2026-10-%02d'%(22+i)))
    check('Pontuação do treino '+str(i+1),abs(saved['pontosTreino']-66)<1e-8)
after=request('/api/atletas/'+str(a['id']))
check('Acumulados corretos',after['totalTreinos']==5 and after['tempoTotalMinutos']==150 and abs(after['pontosTotais']-330)<1e-8)
check('Progressão para INICIANTE_2',after['nivel']=='INICIANTE_2')
h=request(path)
check('Histórico completo e ordenado',len(h)==5 and [x['data'] for x in h]==sorted([x['data'] for x in h],reverse=True))
ranking=request('/api/atletas/ranking')
check('Ranking em ordem decrescente',all(ranking[i]['pontosTotais']>=ranking[i+1]['pontosTotais'] for i in range(len(ranking)-1)))
check('Atleta consta no ranking',any(x['id']==a['id'] and x['pontosTotais']==330 for x in ranking))
check('Respostas não expõem senhas',all('senha' not in x for x in request('/api/atletas')))
print(json.dumps({'resultado':'APROVADO','verificacoes':len(checks),'atleta':name,'id':a['id']},ensure_ascii=False))
