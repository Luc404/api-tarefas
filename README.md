# api-tarefas

API REST de exemplo com **Spring Boot 4.2.0 + Java 21** para a atividade de Arquitetura. Ela roda local com Maven e foi preparada para subir no **AWS Free Tier** com `CodePipeline → CodeBuild → CodeDeploy → EC2`.

## Sumário

- [Visão geral](#visão-geral)
- [Tecnologias](#tecnologias)
- [Estrutura do projeto](#estrutura-do-projeto)
- [Rodando localmente](#rodando-localmente)
- [Endpoints](#endpoints)
- [Testes](#testes)
- [Build de deploy](#build-de-deploy)
- [Deploy na AWS (console, modo free)](#deploy-na-aws-console-modo-free)
- [Cuidados para não pagar nada](#cuidados-para-não-pagar-nada)
- [Solução de problemas](#solução-de-problemas)
- [Commits](#commits)

## Visão geral

O fluxo completo é:

```text
push na branch main
   -> CodePipeline (GitHub App / CodeConnections)
   -> CodeBuild (buildspec.yml)
   -> artefato no S3 (app.jar + appspec.yml + scripts)
   -> CodeDeploy
   -> EC2 com o serviço systemd api-tarefas na porta 8080
```

A API devolve um JSON de status na raiz e uma lista fixa de tarefas em `/tarefas`.

## Tecnologias

- Java 21 (Corretto na AWS)
- Spring Boot 4.2.0
- Maven Wrapper (`mvnw` / `mvnw.cmd`)
- JUnit 5 + `spring-boot-starter-webmvc-test`
- AWS: CodePipeline, CodeBuild, CodeDeploy, EC2, S3, IAM, CloudWatch, Budgets
- GitHub: repositório público `api-tarefas` + GitHub App do CodeConnections

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
│       │   │   ├── DemoApplication.java    # ponto de entrada do Spring
│       │   │   └── TarefasController.java  # rotas da API
│       │   └── resources/application.properties
│       └── test/java/com/example/api_tarefas/DemoApplicationTests.java
├── buildspec.yml                     # comandos do CodeBuild
├── deploy/
│   ├── appsec.yml                    # AppSpec do CodeDeploy (nome de arquivo antigo)
│   └── scripts/
│       ├── instalar.sh               # valida JAR e Java na EC2
│       └── iniciar.sh                # cria e inicia o servico systemd
└── README.md
```

> Observação: o arquivo fonte chama-se `appsec.yml`, mas o `buildspec.yml` o copia para dentro do artefato com o nome `appspec.yml`, que é o nome exigido pelo CodeDeploy. Isso corrige o erro de `cp` que existia antes.

Os arquivos do Maven Wrapper são gerados pelo Spring Initializr e foram mantidos sem comentários extras. Todo o código escrito para este projeto (Java, XML, YAML, properties e shell) tem comentários curtos explicando cada parte. Políticas IAM, quando criadas, serão JSON puro: JSON não aceita comentários, então o motivo de cada permissão fica descrito na seção da AWS abaixo.

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

No Windows, use `mvnw.cmd` no lugar de `./mvnw`.

A API sobe em `http://localhost:8080`.

## Endpoints

### `GET /`

Status simples da aplicação.

```bash
curl http://localhost:8080/
```

Resposta:

```json
{ "status": "ok", "aplicacao": "api-tarefas" }
```

### `GET /tarefas`

Lista de tarefas de demonstração.

```bash
curl http://localhost:8080/tarefas
```

Resposta:

```json
[
  { "id": 1, "titulo": "Estudar Java", "concluida": false },
  { "id": 2, "titulo": "Aprender AWS", "concluida": false }
]
```

## Testes

```bash
cd ApiTarefas/demo
./mvnw test
```

O teste `contextLoads` carrega o contexto Spring. Ele está no pacote `com.example.api_tarefas` de propósito: se o teste ficasse em `com.example.demo`, o Spring não encontraria a configuração.

## Build de deploy

O `buildspec.yml` faz tudo em uma etapa:

1. entra em `ApiTarefas/demo`;
2. roda `./mvnw clean package` (testa e gera o JAR);
3. monta `deploy-package/` com:
   - `app.jar`
   - `appspec.yml`
   - `scripts/instalar.sh`
   - `scripts/iniciar.sh`

Para reproduzir localmente:

```bash
./mvnw clean package
mkdir -p deploy-package/scripts
cp ApiTarefas/demo/target/*.jar deploy-package/app.jar
cp deploy/appsec.yml deploy-package/appspec.yml
cp deploy/scripts/*.sh deploy-package/scripts/
chmod +x deploy-package/scripts/*.sh
```

## Deploy na AWS (console, modo free)

Região usada nos exemplos: `sa-east-1` (São Paulo).

### 1. Conta e proteção de gastos

1. Crie a conta AWS no **Free plan**.
2. Abra **Budgets** → crie um budget de **US$ 5,00** por mês.
3. Adicione alarmes em 80% e 100% para o e-mail da conta.
4. Não faça upgrade para o plano pago.

### 2. Repositório e conexão com GitHub

1. Crie o repositório público `api-tarefas`.
2. Faça push da branch `main`.
3. No CodePipeline, crie uma conexão **GitHub App (CodeConnections)**.
4. Autorize o **AWS Connector for GitHub** apenas no repositório `api-tarefas`.

### 3. IAM (4 roles)

| Role | Uso | Permissões resumidas |
| --- | --- | --- |
| `api-tarefas-codebuild-role` | CodeBuild ler/escrever artefatos e logs | CloudWatch Logs + `s3:GetObject`, `s3:PutObject` no bucket do artefato |
| `api-tarefas-pipeline-role` | CodePipeline orquestrar tudo | S3, CodeBuild, CodeDeploy, `codestar-connections:UseConnection`, `iam:PassRole` |
| `api-tarefas-codedeploy-role` | CodeDeploy publicar na EC2 | política gerenciada `AWSCodeDeployRole` |
| `api-tarefas-ec2-role` | EC2 baixar artefato e rodar o agente | leitura no bucket do artefato, pacote `aws-codedeploy-sa-east-1`, `AmazonSSMManagedInstanceCore` |

### 4. S3

- Bucket: `api-tarefas-artefatos-<ACCOUNT_ID>`
- Versionamento: habilitado
- Lifecycle: expirar objetos após **7 dias**

### 5. EC2

- AMI: **Amazon Linux 2023**
- Tipo: **t3.micro**
- Disco: 8 GB gp3
- Instance profile: `api-tarefas-ec2-role`
- Tag: `Name = api-tarefas`
- Security group:
  - entrada `22` apenas do seu IP;
  - entrada `8080` do seu IP (ou `0.0.0.0/0` somente durante a demo);
  - saída liberada.

User data da instância:

```bash
#!/bin/bash
# Instala Java 21.
dnf install -y java-21-amazon-corretto-headless wget

# Baixa e instala o agente do CodeDeploy v2.
cd /tmp
wget https://aws-codedeploy-sa-east-1.s3.sa-east-1.amazonaws.com/latestv2/install
chmod +x install
./install auto

# Garante que o agente suba junto com a máquina.
systemctl enable --now codedeploy-agent
```

Validação na EC2:

```bash
java -version
systemctl status codedeploy-agent
```

O status esperado do agente é `active (running)` / `Online`.

### 6. CodeBuild

- Nome: `api-tarefas-build`
- Source: **CodePipeline**
- Environment image: `aws/codebuild/amazonlinux-x86_64-standard:5.0`
- Compute: `BUILD_GENERAL1_SMALL`
- Buildspec: `buildspec.yml` na raiz
- Role: `api-tarefas-codebuild-role`
- Timeout: 20 minutos
- Artifacts: **CodePipeline**

### 7. CodeDeploy

- Application: `api-tarefas`
- Compute platform: **EC2/On-premises**
- Deployment group: `api-tarefas-dg`
- Service role: `api-tarefas-codedeploy-role`
- Deployment type: **In-place**
- Configuração: `CodeDeployDefault.OneAtATime`
- Seletor: tag `Name = api-tarefas`
- Load balancer: **nenhum**

### 8. CodePipeline

- Nome: `api-tarefas-pipeline`
- Service role: `api-tarefas-pipeline-role`
- Source: **GitHub App (CodeConnections)**, repo `api-tarefas`, branch `main`, webhook habilitado
- Build: `api-tarefas-build`
- Deploy: CodeDeploy → `api-tarefas` / `api-tarefas-dg`

### 9. Validação final

Faça um pequeno push na `main`, acompanhe os eventos do pipeline e teste:

```bash
curl http://<IP_PUBLICO>:8080/
curl http://<IP_PUBLICO>:8080/tarefas
```

Depois da demonstração, **termine a instância EC2** para consumir o mínimo de crédito.

## Cuidados para não pagar nada

- 1 pipeline ativa por mês (limite free).
- CodeBuild em `BUILD_GENERAL1_SMALL` (100 min/mês grátis).
- CodeDeploy em EC2 não tem cobrança adicional.
- EC2 `t3.micro` usando os créditos do Free Plan.
- Sem ALB, NAT Gateway, RDS, EIP reservada, domínio ou segundo pipeline.
- Budget de US$ 5 com alarme por e-mail.
- Terminar a EC2 ao final da atividade.

## Solução de problemas

**`Unable to find a @SpringBootConfiguration`**
O teste estava em `com.example.demo` enquanto a aplicação estava em `com.example.api_tarefas`. Corrigido movendo o teste para o mesmo pacote.

**`404` em `/` e `/tarefas`**
A classe `DemoApplication` estava em `com.example.tarefas`, então o Spring não escaneava o controller. Corrigido para `com.example.api_tarefas`.

**`cp: não foi possível obter estado de .../deploy/appspec.yml`**
O arquivo no repositório chama-se `deploy/appsec.yml`. O `buildspec.yml` agora copia esse nome para `deploy-package/appspec.yml`.

**VS Code marca imports como erro**
Às vezes o Java Language Server guarda cache de pacotes antigos. Foi feito `Clean Java Language Server Workspace`; se voltar, rode novamente essa limpeza e aguarde o Maven reimportar.

## Commits

Os commits foram feitos em etapas pequenas e explicadas:

1. `feat: adiciona API REST de tarefas com Spring Boot e teste` — código Java, `pom.xml`, properties e wrapper.
2. `build: adiciona buildspec e scripts de build/deploy` — `buildspec.yml`, `deploy/appsec.yml` e scripts.
3. `docs: adiciona README completo e .gitignore da raiz` — documentação e exclusões do repositório.
