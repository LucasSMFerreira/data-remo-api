"""Envia 5 pacotes fictícios com o mesmo contrato do ESP32. Só aceita API local."""
import argparse, json, time, uuid
from urllib.request import Request, urlopen
from urllib.parse import urlparse
parser=argparse.ArgumentParser(description=__doc__)
parser.add_argument('--treino-id',type=int,required=True)
parser.add_argument('--url',default='http://127.0.0.1:8080')
args=parser.parse_args()
if urlparse(args.url).hostname not in ('localhost','127.0.0.1'):
    parser.error('Use somente uma API local para esta simulação.')
batch=uuid.uuid4().hex
for i in range(5):
    body=dict(eventoId=f'python-{batch}-{i}',dispositivoId='ESP32-DEMO-PYTHON',treinoId=args.treino_id,tempoDecorridoMs=i*5000,latitude=-23.5505+i*.0001,longitude=-46.6333+i*.0001,velocidadeKmh=7+i*.5,aceleracaoX=.1+i*.02,aceleracaoY=.05,aceleracaoZ=9.81,simulacao=True)
    req=Request(args.url+'/api/telemetria',data=json.dumps(body).encode(),headers={'Content-Type':'application/json'})
    with urlopen(req,timeout=15) as response:
        saved=json.load(response)
        print(f"Confirmado {i+1}/5: {saved['eventoId']} | {saved['velocidadeKmh']} km/h | {saved['origem']}")
    if i<4: time.sleep(5)
