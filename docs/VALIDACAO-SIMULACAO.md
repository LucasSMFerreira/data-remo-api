# Validação ponta a ponta da simulação — 26/10/2026

Escopo: aplicação local, API Java e PostgreSQL. Os dados são fictícios. Validação de bancada; não comprova precisão de sensores nem prontidão de produção.

## Resultado

- Compilação/empacotamento Maven: aprovados.
- Teste de contexto Spring: 1 aprovado.
- `python scripts/test-functional.py`: 37 verificações aprovadas, incluindo cadastro inválido/duplicado, login, treino inválido, pontos, níveis, histórico e ranking.
- `python scripts/test-telemetry.py`: 21 verificações aprovadas, incluindo comparação de todos os campos gravados, reenvio sem duplicação, conflito de evento, faixas, vínculo ao treino, ordenação, limite e ausência de alteração nos pontos.
- Navegador: cadastro com idade negativa recusado; cadastro válido de usuário novo; orientação sem treino; criação de treino; envio e conferência de 5 medições; reenvio mantendo 5 registros; exportação JSON validada campo a campo.
- Reinício: PostgreSQL e API reiniciados. A consulta e a conferência do lote continuaram disponíveis; logout, senha incorreta e login correto testados novamente.
- Consulta SQL direta no cenário visual: 5 medições, velocidade 7..9 km/h, tempo 0..20000 ms; atleta com 1 treino, 30 minutos e 66 pontos.

## Como repetir pela interface

1. Abra a aplicação, cadastre um atleta fictício e entre.
2. No painel, selecione Telemetria do hardware · simulação. Sem treinos, a mensagem explica o requisito e o link Registrar treino leva ao formulário.
3. Crie um treino: 30 minutos, 5 km, intensidade Moderada e tipo Base. O painel deve mostrar 66 pontos.
4. Abra Telemetria, aguarde o treino carregar e clique Enviar 5 dados fictícios.
5. Confira a mensagem 5/5: identificadores, treino, tempo, coordenadas, velocidade e aceleração devem coincidir com os pacotes enviados.
6. Clique Reenviar último lote: a contagem deve permanecer em 5.
7. Clique Baixar dados JSON para inspecionar os valores sem arredondamento de apresentação.
8. Recarregue e clique Conferir último envio. A API consulta cada evento por ID, inclusive se ele tiver saído da lista das últimas 50 medições.

Cada novo clique em Enviar 5 dados fictícios cria um novo lote e adiciona cinco medições. Reenviar último lote conserva os identificadores. O último lote do navegador fica no armazenamento local; limpar esse armazenamento remove a referência local, mas não os dados no PostgreSQL. Esta versão guarda somente o último lote por treino no navegador.

## Melhorias desta revisão

- Email, idade e senha validados no cadastro; identificadores e acumulados recebidos do cliente não são usados para iniciar uma conta.
- Tipos/intensidades inválidos e notas longas são recusados; atleta inexistente retorna 404.
- Duração fracionária recusada pelo formulário e data inicial usa o calendário local.
- Mensagem de recuperação de senha deixa explícito que não há envio de email.
- Adicionados conferência por leitura da API, reenvio e exportação JSON na telemetria.

## Limites que continuam

Sem placa/sensores físicos nesta sessão; firmware ainda não compilado para um modelo de ESP32. A velocidade e a posição não foram avaliadas em movimento real. Login local ainda não estabelece autorização das rotas. HTTPS, autenticação de dispositivos, migrações e testes de concorrência/carga permanecem fora desta validação. A API continua vinculada a 127.0.0.1. Não houve merge na main.
