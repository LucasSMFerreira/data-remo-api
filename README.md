# Data Remo — API de Gestão de Treinos de Remo

Protótipo acadêmico em Java para cadastrar atletas, registrar treinos e remadas e consultar indicadores de desempenho e ranking.

**Tecnologias:** Java 21 · Spring Boot 4.0.5 · Spring Web MVC · Spring Data JPA · PostgreSQL · Maven.

## O que este projeto demonstra

- Organização em controllers, serviços, repositórios, DTOs e entidades.
- Persistência relacional com JPA e consultas por atleta e período.
- Cálculo de pontuação e resumo mensal das remadas.
- Exercícios de padrões de projeto, com classes de fábrica, observadores e decoradores no domínio.

## Estado atual

O repositório contém o backend e um ponto de entrada de console. Não inclui uma interface web nem configuração de banco pronta para uso. A execução depende de preparar um PostgreSQL local e validar a inicialização do esquema.

É um projeto em evolução: a documentação descreve o código disponível, sem afirmar validação de execução ou cobertura funcional completa.

## Rotas implementadas

| Método | Rota | Objetivo |
|---|---|---|
| GET | `/api/ping` | Retornar `ok` |
| GET | `/api/atletas` | Listar atletas |
| POST | `/api/atletas` | Cadastrar atleta |
| POST | `/api/atletas/login` | Verificar nome e senha |
| GET | `/api/atletas/ranking` | Ranking de atletas com pontuação positiva |
| POST | `/api/treinos` | Registrar treino pelo DTO |
| POST | `/api/remadas` | Registrar remada e atualizar acumulados do atleta |
| GET | `/api/remadas/atleta/{nome}` | Consultar remadas do atleta |
| GET | `/api/remadas/atleta/{nome}/semana/{semana}` | Filtrar por semana |
| GET | `/api/remadas/atleta/{nome}/mes/{mes}/{ano}` | Filtrar por mês |
| GET | `/api/remadas/atleta/{nome}/resumo-mensal/{mes}/{ano}` | Consolidar treinos, minutos e pontos |

As rotas de treinos e remadas têm modelos distintos. A verificação de login atual não estabelece, por si só, autorização para as demais rotas.

## Preparar o ambiente local

Requisitos: **JDK 21**, Git e PostgreSQL. O Maven Wrapper está incluído; o primeiro uso precisa de acesso à internet para baixar dependências.

```powershell
git clone https://github.com/LucasSMFerreira/data-remo-api.git
cd data-remo-api
java -version
```

Crie um banco descartável de desenvolvimento chamado `data_remo`. No PowerShell, configure a conexão com suas credenciais locais:

```powershell
$env:SPRING_DATASOURCE_URL = "jdbc:postgresql://localhost:5432/data_remo"
$env:SPRING_DATASOURCE_USERNAME = "seu_usuario_local"
$env:SPRING_DATASOURCE_PASSWORD = "sua_senha_local"
$env:SPRING_JPA_HIBERNATE_DDL_AUTO = "update"
.\mvnw.cmd spring-boot:run
```

Em Linux/macOS, use as variáveis equivalentes do shell e `sh mvnw spring-boot:run`.

**Limitação de inicialização:** `config/SchemaFix.java` executa alterações diretamente na tabela `atletas`. Em um banco vazio, a ordem entre esse componente e a criação das tabelas pelo Hibernate precisa ser validada; se ocorrer erro de tabela inexistente, a inicialização deve ser corrigida antes de considerar o roteiro reproduzível. A opção `update` é apenas uma configuração de desenvolvimento, sem substituir migrações versionadas.

Depois de a aplicação iniciar com sucesso, a porta padrão do Spring Boot é 8080:

```powershell
Invoke-RestMethod http://localhost:8080/api/ping
```

Resposta esperada da rota: `ok`.

## Estrutura

```text
src/main/java/com/dataremo/Data_Remo/
  controller/    Rotas HTTP
  service/       Serviços de treino e módulos do domínio
  repository/    Repositórios e acesso a dados
  model/         Entidades, relatórios e padrões de projeto
  dto/           Entrada de treino
  config/        Ajustes de esquema
  console/       Entrada de console
src/test/        Teste de carregamento do contexto Spring
pom.xml          Dependências e configuração de compilação
```

## Testes

Com o banco de desenvolvimento configurado:

```powershell
.\mvnw.cmd test
```

O teste versionado atualmente verifica o carregamento do contexto. Ele não comprova os contratos das rotas nem os cálculos do domínio e depende da configuração externa do banco.

## Limites e próximas melhorias

1. Substituir comparação direta de senhas por hash e separar entidades de respostas HTTP: o modelo atual expõe um getter de senha e os controllers retornam entidades.
2. Implementar autenticação e autorização das rotas antes de qualquer exposição pública da aplicação.
3. Versionar migrações e garantir inicialização em banco vazio.
4. Adicionar validação de entrada, tratamento consistente de erros e testes de serviços e endpoints.
5. Preparar demonstração com dados fictícios e documentação dos corpos de requisição.

Use somente dados fictícios e senhas descartáveis no ambiente local. Publicar o código não significa que a API esteja preparada para receber dados reais.

## Licença

Este repositório ainda não contém uma licença de uso explícita.
