# Primeiro envio fictício do ESP32 ao DataRemo

Estado: protótipo de bancada. A API e a tela foram testadas com envio HTTP pelo computador. O sketch precisa ser compilado para a placa exata e validado no hardware. Não contém código de leitura de GPS ou MPU6050.

## 1. Arquitetura

ESP32 → Wi-Fi → POST /api/telemetria → Java/Spring Boot/JPA → PostgreSQL → telemetria.html.

Nesta etapa, todas as medições têm origem SIMULACAO e pertencem a um treino existente. A API não recebe medições reais (`simulacao=false` é recusado). Medir não cria treinos nem altera seus pontos, duração ou distância.

## 2. Primeiro teste sem placa

Inicie o backend com PostgreSQL configurado. Entre na aplicação, registre um treino e abra o botão “Telemetria do hardware · simulação” no painel. O seletor mostra o ID, por exemplo #1.

```powershell
python scripts/send-telemetry-demo.py --treino-id 1
```

Troque 1 pelo ID escolhido. O script envia cinco pacotes com intervalo de cinco segundos e mostra a confirmação de cada envio. A tela consulta a API a cada cinco segundos. O botão “Enviar 5 dados fictícios” envia um lote de demonstração pelo navegador; não representa uma placa conectada.

## 3. Contrato de envio

```json
{
  "eventoId": "esp32-sessao1-0",
  "dispositivoId": "ESP32-DEMO",
  "treinoId": 1,
  "tempoDecorridoMs": 5000,
  "latitude": -23.5505,
  "longitude": -46.6333,
  "velocidadeKmh": 8.0,
  "aceleracaoX": 0.10,
  "aceleracaoY": 0.05,
  "aceleracaoZ": 9.81,
  "simulacao": true
}
```

Todos os campos são obrigatórios. Coordenadas em graus, velocidade em km/h e aceleração em m/s². Tempo decorrido é fictício, em milissegundos; `recebidoEm` é definido pelo servidor em UTC, exibido na hora local pelo navegador. Os identificadores aceitam de 1 a 64 letras ASCII, dígitos, hífen ou sublinhado. Faixas desta demonstração: latitude -90..90, longitude -180..180, velocidade 0..200, cada aceleração -200..200; números devem ser finitos. Esses limites não são uma calibração dos sensores.

POST /api/telemetria retorna 200 com a medição salva. Reenviar o mesmo eventoId e conteúdo retorna a confirmação sem criar outra linha. Outro conteúdo com o mesmo eventoId retorna 409. Pacotes inválidos retornam 400; treino inexistente retorna 404. Em um conflito de gravações simultâneas, reenviar o mesmo pacote confirma o estado persistido.

GET /api/telemetria/treino/{id} retorna até as 50 medições mais recentes, em ordem de recebimento. A tabela PostgreSQL `medicoes_telemetria` tem chave primária evento_id e chave estrangeira treino_id. Nesta fase, Hibernate `update` cria a tabela no banco de desenvolvimento; migrações versionadas continuam pendentes.

## 4. Preparar a placa

1. Abra `esp32/DataRemoTelemetria/DataRemoTelemetria.ino` no Arduino IDE com o pacote de placas `esp32 by Espressif Systems` instalado. Selecione o modelo exato e a porta USB. Alternativamente, reutilize o projeto DataRemo_Inicio no Arduino Cloud, copiando o código e adicionando o cabeçalho de configuração.
2. Copie `secrets.example.h` para `secrets.h` na pasta do sketch. Preencha SSID, senha, URL e TREINO_ID. `secrets.h` está ignorado pelo Git; não publique senhas.
3. Use uma rede Wi-Fi compatível com sua placa. A URL precisa usar o IP do computador acessível pela placa; `127.0.0.1` na placa aponta para a própria placa.
4. O ambiente atual do projeto está vinculado a `127.0.0.1`, portanto AINDA NÃO recebe conexões da placa. Para o teste físico, configure o endereço de escuta no IP privado do computador, inicie o backend com essa configuração e, se necessário, permita a porta 8080 apenas na rede privada e para o IP da placa. Não exponha o PostgreSQL: só a API recebe conexões. Essa alteração de rede não foi feita automaticamente.
5. Compile, envie e abra o Monitor Serial em 115200 baud. São esperadas cinco confirmações HTTP 200. Em falhas transitórias, o sketch mantém o pacote e tenta novamente; erros de formato ou treino interrompem o envio.
6. Confira as cinco medições na tela. Reiniciar a placa cria uma sessão de simulação nova. A versão de bancada não mantém a fila se faltar energia.

O Arduino Cloud Editor é uma ferramenta para editar e carregar o programa; não é necessário passar pela nuvem Arduino para enviar dados à nossa API. A primeira comunicação usa HTTP em rede local isolada com dados fictícios. Autenticação de dispositivos, HTTPS, gerenciamento de sessões e coleta real são etapas futuras, antes de qualquer exposição externa.

## 5. Verificação e versionamento

```powershell
python scripts/test-telemetry.py
git status
git diff
git log -2 --oneline
```

Os testes criam atletas, treinos e medições fictícias, que permanecem no banco local. Eles verificam validações, reenvios, conflito de identificador, isolamento por treino, limite/ordem e que as medições não alteram o ranking.

Trabalhamos na branch `feature/consolidar-web-backend`. Mantenha o firmware no mesmo repositório para que API, interface e contrato sejam revisados juntos. Referências: https://docs.arduino.cc/arduino-cloud/guides/esp32 e https://github.com/espressif/arduino-esp32/blob/master/libraries/HTTPClient/examples/BasicHttpClient/BasicHttpClient.ino.

## Resultado verificado em 26/10/2026

- Compilação Java e empacotamento: aprovados.
- Teste Spring: 1 aprovado, sem falhas.
- Testes funcionais existentes: 29 verificações aprovadas.
- Testes HTTP de telemetria: 19 verificações aprovadas.
- Navegador: estado vazio, cinco envios do script, mais cinco do botão e persistência após reiniciar API/PostgreSQL confirmados. Sem erros no console na inspeção final.
- Firmware: arquivo preparado e revisado; sem compilação para placa nem teste físico nesta sessão. O ambiente não tem `arduino-cli` disponível.
- A API permaneceu acessível somente no computador (`127.0.0.1`); nenhuma regra de firewall foi alterada.
