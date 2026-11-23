# api-tarefas

API REST de exemplo com **Spring Boot 4.2.0 + Java 21** para a atividade de Arquitetura. Ela roda local com Maven e inclui uma camada que **simula** o fluxo de CodePipeline, CodeBuild, S3, CodeDeploy, EC2 e IAM.

**Nada é criado em serviço externo.** Os “recursos da AWS” são objetos em memória dentro da própria aplicação.

Para o passo a passo completo e o motivo de cada escolha, veja [ARQUITETURA.md](ARQUITETURA.md).

## Sumário

- [Visão geral](#visão-geral)
- [Arquitetura detalhada](ARQUITETURA.md)
- [Tecnologias](#tecnologias)
- [Estrutura do projeto](#estrutura-do-projeto)
- [Rodando localmente](#rodando-localmente)
- [Endpoints da API](#endpoints-da-api)
- [Endpoints da simulação](#endpoints-da-simulação)
- [Como a simulação funciona](#como-a-simulação-funciona)
- [Arquivos ilustrativos](#arquivos-ilustrativos)
- [Testes](#testes)
- [Build](#build)
- [Solução de problemas](#solução-de-problemas)
- [Commits](#commits)

## Visão geral

A simulação reproduce este caminho:

```text
POST /aws/pipeline/run
   -> CodeBuild simulado   (status SUCESSO)
   -> S3 simulado          (app.jar + appspec.yml em memória)
   -> CodeDeploy simulado  (status SUCESSO)
   -> EC2 simulada         (estado RODANDO)
```

O estado pode ser consultado em `GET /aws/status`. Tudo some quando a aplicação para, porque não há persistência nem chamada externa.

## Tecnologias

- Java 21
- Spring Boot 4.2.0
- Maven Wrapper (`mvnw` / `mvnw.cmd`)
- JUnit 5 + `spring-boot-starter-webmvc-test`
- Spring MVC para expor os endpoints JSON

## Estrutura do projeto

```text
.
├── ApiTarefas/demo/
│   ├── .mvn/wrapper/                 # configuracao do Maven Wrapper (gerado)
│   ├── mvnw / mvnw.cmd               # scripts gerados para rodar o Maven
│   ├── pom.xml                       # dependencias e versao do Java
│   └── src/
│       ├── main/
│       │   ├── java/com/example/api_tarefas/
│       │   │   ├── DemoApplication.java       # ponto de entrada do Spring
│       │   │   ├── TarefasController.java     # rotas da API de tarefas
│       │   │   └── aws/
│       │   │       ├── AwsSimulationController.java # rotas da simulacao
│       │   │       └── AwsSimulationService.java    # estado simulado em memoria
│       │   └── resources/application.properties
│       └── test/java/com/example/api_tarefas/
│           ├── DemoApplicationTests.java
│           └── aws/AwsSimulationServiceTest.java
├── buildspec.yml                     # roteiro ilustrativo de build
├── deploy/
│   ├── appsec.yml                    # AppSpec ilustrativo
│   └── scripts/
│       ├── instalar.sh               # script ilustrativo
│       └── iniciar.sh                # script ilustrativo
└── README.md
```

Todo o código escrito para este projeto tem comentários curtos explicando cada parte. Os arquivos do Maven Wrapper são gerados pelo Spring Initializr e foram mantidos sem comentários extras.

## Rodando localmente

Pré-requisito: **JDK 21**. O Maven Wrapper baixa o Maven automaticamente.

```bash
cd ApiTarefas/demo
./mvnw clean package
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

Alternativa para desenvolvimento:

```bash
cd ApiTarefas/demo
./mvnw spring-boot:run
```

No Windows, use `mvnw.cmd`. A aplicação sobe em `http://localhost:8080`.

## Endpoints da API

### `GET /`

```bash
curl http://localhost:8080/
```

```json
{ "status": "ok", "aplicacao": "api-tarefas" }
```

### `GET /tarefas`

```bash
curl http://localhost:8080/tarefas
```

```json
[
  { "id": 1, "titulo": "Estudar Java", "concluida": false },
  { "id": 2, "titulo": "Aprender AWS", "concluida": false }
]
```

## Endpoints da simulação

### `GET /aws/status`

Mostra o estado atual dos recursos simulados, antes ou depois de executar o pipeline.

```bash
curl http://localhost:8080/aws/status
```

### `POST /aws/pipeline/run`

Executa o pipeline simulado de ponta a ponta.

```bash
curl -X POST http://localhost:8080/aws/pipeline/run
```

Resposta resumida:

```json
{
  "ambiente": "SIMULADO",
  "codepipeline": { "nome": "api-tarefas-pipeline", "status": "CONCLUIDO" },
  "codebuild": { "nome": "api-tarefas-build", "status": "SUCESSO" },
  "s3": {
    "bucket": "api-tarefas-artefatos-simulado",
    "artefatos": {
      "app.jar": "JAR simulado gerado pelo build",
      "appspec.yml": "AppSpec simulado"
    }
  },
  "codedeploy": { "aplicacao": "api-tarefas", "grupo": "api-tarefas-dg", "status": "SUCESSO" },
  "ec2": { "nome": "api-tarefas", "tipo": "t3.micro", "estado": "RODANDO (simulado)" },
  "iam": {
    "papeis": [
      "api-tarefas-codebuild-role",
      "api-tarefas-pipeline-role",
      "api-tarefas-codedeploy-role",
      "api-tarefas-ec2-role"
    ]
  }
}
```

## Como a simulação funciona

| Recurso simulado | Onde está | O que faz |
| --- | --- | --- |
| CodePipeline | `AwsSimulationService` | Muda o status de `PRONTO` para `EXECUTANDO` e depois `CONCLUIDO`. |
| CodeBuild | `AwsSimulationService` | Marca o build como `SUCESSO`. |
| S3 | Mapa `artefatos` | Guarda `app.jar` e `appspec.yml` apenas em memória. |
| CodeDeploy | `AwsSimulationService` | Marca o deploy como `SUCESSO`. |
| EC2 | `AwsSimulationService` | Mostra a instância como `RODANDO (simulado)` após o deploy. |
| IAM | Lista `papeis` | Mostra os nomes dos papéis que fariam parte do fluxo. |

## Arquivos ilustrativos

`buildspec.yml`, `deploy/appsec.yml`, `deploy/scripts/iniciar.sh` e `deploy/scripts/instalar.sh` mostram como seria o formato de build e deploy. Eles **não são executados pela simulação** e possuem comentários dizendo isso.

O arquivo fonte chama-se `appsec.yml`, mas o `buildspec.yml` o copiaria para `appspec.yml`, que é o nome exigido pelo formato de deploy. Isso apenas documenta a correção feita no projeto.

## Testes

```bash
cd ApiTarefas/demo
./mvnw test
```

- `DemoApplicationTests`: verifica se o contexto Spring carrega.
- `AwsSimulationServiceTest`: verifica se o pipeline simulado termina com sucesso e gera o artefato `app.jar`.

## Build

```bash
cd ApiTarefas/demo
./mvnw clean package
```

O comando gera `target/demo-0.0.1-SNAPSHOT.jar`.

## Solução de problemas

**`Unable to find a @SpringBootConfiguration`**
O teste estava em `com.example.demo` enquanto a aplicação estava em `com.example.api_tarefas`. Corrigido movendo o teste para o mesmo pacote.

**`404` em `/` e `/tarefas`**
A classe `DemoApplication` estava em `com.example.tarefas`, então o Spring não escaneava o controller. Corrigido para `com.example.api_tarefas`.

**`cp: não foi possível obter estado de .../deploy/appspec.yml`**
O arquivo no repositório chama-se `deploy/appsec.yml`. O `buildspec.yml` copia esse nome para `appspec.yml` no roteiro ilustrativo.

**VS Code marca imports como erro**
Às vezes o Java Language Server guarda cache de pacotes antigos. Use `Clean Java Language Server Workspace` e aguarde o Maven reimportar.

## Commits

O histórico mostra as etapas, incluindo a versão anterior com arquivos de AWS real que depois foi substituída:

1. `feat: adiciona API REST de tarefas com Spring Boot e teste`
2. `build: adiciona buildspec e scripts de build/deploy`
3. `docs: adiciona README completo e .gitignore da raiz`
4. `chore: adiciona politicas IAM e user-data da EC2` — etapa anterior, removida depois.
5. `docs: fixa Account ID e nomes AWS no README` — etapa anterior, substituída depois.
6. `refactor: substitui AWS real por simulacao local`
7. `docs: reescreve README para simulacao sem recursos externos`
