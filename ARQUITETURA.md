# Arquitetura e explicação do sistema

Este documento explica **o que o sistema faz** e **por que cada parte existe**. Ele é o material de estudo da atividade: o código mostra a implementação, aqui está o raciocínio por trás dela.

## 1. Objetivo

O projeto é uma API REST pequena em Spring Boot que serve para demonstrar duas coisas ao mesmo tempo:

1. uma API real, simples, com rotas HTTP e respostas JSON;
2. uma **simulação local** de um fluxo de entrega contínua parecido com CodePipeline → CodeBuild → S3 → CodeDeploy → EC2.

A simulação não usa SDK, não cria recursos e não faz chamadas externas. Ela existe apenas para representar a arquitetura de forma didática.

## 2. O que o sistema faz

O sistema oferece cinco rotas HTTP:

| Rota | O que faz |
| --- | --- |
| `GET /` | Devolve um JSON de status dizendo que a aplicação está no ar. |
| `GET /tarefas` | Devolve uma lista fixa de duas tarefas. |
| `GET /aws/status` | Mostra o estado atual dos recursos simulados. |
| `POST /aws/pipeline/run` | Executa o pipeline simulado e atualiza o estado. |
| (implícito) erro 404 | O Spring devolve 404 para rotas que não existem. |

Ao iniciar, o estado simulado é:

```json
{
  "ambiente": "SIMULADO",
  "codepipeline": { "status": "PRONTO" },
  "codebuild": { "status": "NAO_EXECUTADO" },
  "codedeploy": { "status": "NAO_EXECUTADO" },
  "ec2": { "estado": "PARADO (simulado)" }
}
```

Depois de `POST /aws/pipeline/run`, o estado passa a:

```json
{
  "codepipeline": { "status": "CONCLUIDO" },
  "codebuild": { "status": "SUCESSO" },
  "codedeploy": { "status": "SUCESSO" },
  "ec2": { "estado": "RODANDO (simulado)" },
  "s3": {
    "artefatos": ["app.jar", "appspec.yml"]
  }
}
```

## 3. O que o sistema NÃO faz

Isso é importante para não confundir a demonstração:

- não cria instância, bucket, role ou pipeline em nenhum serviço externo;
- não usa credenciais, token ou conta de nuvem;
- não grava em banco de dados;
- não persiste estado depois que a aplicação para;
- não chama AWS SDK;
- não executa os scripts de deploy;
- não possui autenticação, autorização ou usuários.

Tudo o que parece “AWS” são nomes e estados guardados em memória.

## 4. Tecnologias e por que foram usadas

| Tecnologia | Por quê |
| --- | --- |
| Java 21 | Versão atual e compatível com o ecossistema Spring usado na atividade. |
| Spring Boot 4.2.0 | Facilita criar API REST: configura servidor web, JSON e injeção de dependência automaticamente. |
| Spring MVC | Faz o mapeamento de URLs para métodos Java (`@GetMapping`, `@PostMapping`). |
| Maven Wrapper | Garante que qualquer pessoa rode o projeto com a mesma versão do Maven, sem instalar Maven separadamente. |
| JUnit 5 | Testa o contexto Spring e a lógica da simulação. |
| `Map` e `List` do Java | Permitem montar JSON diretamente, sem criar várias classes só para transportar dados. |

## 5. Estrutura em camadas

```text
Requisição HTTP
      |
      v
Controllers  (TarefasController, AwsSimulationController)
      |
      v
Serviço      (AwsSimulationService)
      |
      v
Estado em memória (Maps, Lists e Strings)
```

### Camada de configuração

- `DemoApplication.java`: inicializa o Spring.
- `application.properties`: define nome da aplicação e porta `8080`.

### Camada de apresentação/HTTP

- `TarefasController.java`: rotas da API de tarefas.
- `AwsSimulationController.java`: rotas da simulação.

Essa camada só recebe a requisição e devolve a resposta. Ela não guarda estado.

### Camada de serviço

- `AwsSimulationService.java`: guarda o estado simulado e executa o pipeline simulado.

Ficar separado do controller permite testar a lógica sem subir o servidor.

### Camada de teste

- `DemoApplicationTests.java`: verifica se a aplicação carrega.
- `AwsSimulationServiceTest.java`: verifica se a simulação funciona.

## 6. Componentes Java um a um

### `DemoApplication.java`

É a classe principal. A anotação `@SpringBootApplication` faz três papéis: configuração, autoconfiguração e varredura de componentes.

Por causa da varredura, o Spring encontra automaticamente:
- `TarefasController`;
- `AwsSimulationController`;
- `AwsSimulationService`.

O método `main` chama `SpringApplication.run`, que cria o contexto, inicia o servidor web embutido e mantém a aplicação rodando.

### `TarefasController.java`

É a API “de negócio” do exemplo:

- `GET /` devolve `{ "status": "ok", "aplicacao": "api-tarefas" }`;
- `GET /tarefas` devolve duas tarefas fictícias.

`@RestController` significa que os retornos dos métodos viram JSON no corpo da resposta.

`Map.of` e `List.of` foram escolhidos porque criam estruturas imutáveis e bem enxutas para dados fixos. O tipo `Map<String, Object>` aparece em `/tarefas` porque cada tarefa mistura texto, número e booleano.

### `AwsSimulationService.java`

É o coração da simulação. Ele guarda:

- `artefatos`: um mapa com `app.jar` e `appspec.yml`;
- `statusPipeline`;
- `statusBuild`;
- `statusDeploy`.

O método `executarPipeline` muda esses campos em ordem, simulando as etapas de um deploy. O método `status` monta um JSON com um bloco para cada recurso simulado: CodePipeline, CodeBuild, S3, CodeDeploy, EC2 e IAM.

`LinkedHashMap` é usado para manter a ordem de inserção, deixando o JSON previsível e mais fácil de ler.

### `AwsSimulationController.java`

É a interface HTTP da simulação:

- `GET /aws/status` consulta o estado;
- `POST /aws/pipeline/run` dispara a execução.

`@RequestMapping("/aws")` define o prefixo das duas rotas. O serviço é recebido no construtor por injeção de dependência, o que deixa a classe testável e evita criar o serviço manualmente.

### `DemoApplicationTests.java`

É um teste de contexto. Com `@SpringBootTest`, o Spring monta a aplicação durante o teste. Se algum bean estiver com configuração errada, o teste quebra.

Ele não precisou de asserção: o simples fato de carregar sem exceção já é a validação.

### `AwsSimulationServiceTest.java`

É um teste unitário da simulação. Ele cria o serviço com `new`, chama `executarPipeline` e verifica duas coisas:

1. o status final do pipeline é `CONCLUIDO`;
2. o mapa de artefatos contém `app.jar`.

O `@SuppressWarnings("unchecked")` existe porque o JSON é representado por `Map<String, Object>` e os casts para mapas internos geram aviso de tipo.

## 7. Fluxo de execução

### 7.1 Inicialização

1. O Java executa `DemoApplication.main`.
2. O Spring lê `@SpringBootApplication`.
3. O Spring faz varredura do pacote `com.example.api_tarefas`.
4. Controllers e serviço são registrados como beans.
5. O servidor web sobe na porta definida em `application.properties`.

### 7.2 `GET /`

1. O Spring chama `TarefasController.status`.
2. O método devolve um `Map`.
3. O conversor JSON do Spring escreve a resposta.

### 7.3 `GET /tarefas`

1. O Spring chama `TarefasController.ListarTarefas`.
2. O método devolve uma `List` de `Map`.
3. O conversor JSON transforma cada `Map` em um objeto do array.

### 7.4 `GET /aws/status`

1. O Spring chama `AwsSimulationController.status`.
2. O controller delega para `AwsSimulationService.status`.
3. O serviço monta o mapa com o estado atual e devolve.
4. Nada é alterado.

### 7.5 `POST /aws/pipeline/run`

1. O Spring chama `AwsSimulationController.executar`.
2. O controller chama `AwsSimulationService.executarPipeline`.
3. O serviço muda os status em ordem e cria os artefatos.
4. O serviço chama `status()` e devolve o estado final.

## 8. Decisões de projeto

| Decisão | Motivo |
| --- | --- |
| Separar controller e serviço | Quem fala HTTP não precisa saber como o estado é guardado. |
| Usar `POST` para executar o pipeline | A chamada altera estado, então não deve ser um `GET`. |
| Guardar estado em memória | É suficiente para simulação e eliminates dependência externa. |
| Usar `Map` no retorno | Evita criar DTOs só para responder JSON. |
| Usar `LinkedHashMap` | Mantém a ordem das chaves no JSON. |
| Usar injeção no construtor | Deixa a dependência explícita e fácil de testar. |
| Usar dois controllers | Separa “tarefas” de “simulação”, cada um com sua responsabilidade. |
| Testar contexto e serviço | Um teste garante que a app sobe; o outro garante a regra da simulação. |
| Marcar arquivos de deploy como ilustrativos | Evita confundir o leitor: eles mostram o formato, mas não são executados. |

## 9. Arquivos não Java e por que existem

| Arquivo | Função |
| --- | --- |
| `pom.xml` | Declara Java 21, Spring Boot Web, dependência de teste e plugin que gera o JAR. |
| `application.properties` | Define `spring.application.name=demo` e `server.port=8080`. |
| `mvnw`, `mvnw.cmd`, `.mvn/wrapper/` | Permitem rodar o Maven sem instalá-lo. |
| `buildspec.yml` | Roteiro ilustrativo de como seria o build e a montagem do artefato. |
| `deploy/appsec.yml` | AppSpec ilustrativo, mostra o formato de hooks de deploy. |
| `deploy/scripts/iniciar.sh` | Mostra como seria a criação de um serviço systemd. |
| `deploy/scripts/instalar.sh` | Mostra como seria a validação do JAR e do Java. |
| `.gitignore` | Evita versionar `target/`, `deploy-package/`, IDEs, logs e segredos. |
| `README.md` | Guia rápido de execução e endpoints. |
| `ARQUITETURA.md` | Este documento: explicação completa e motivos das escolhas. |

## 10. Estado e limitações

O estado da simulação vive apenas na memória do processo. Isso tem consequências:

- reiniciar a aplicação volta tudo ao estado inicial;
- duas instâncias rodando ao mesmo tempo teriam estados separados;
- não há histórico de execuções;
- não há concorrência tratada de forma sofisticada.

Essas limitações são aceitáveis porque o objetivo é didático. Se fosse um sistema real, o próximo passo seria trocar o estado em memória por um banco ou serviço externo, além de adicionar autenticação e observabilidade.

## 11. Como validar

```bash
cd ApiTarefas/demo
./mvnw clean package
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

Em outro terminal:

```bash
curl http://localhost:8080/
curl http://localhost:8080/tarefas
curl http://localhost:8080/aws/status
curl -X POST http://localhost:8080/aws/pipeline/run
curl http://localhost:8080/aws/status
```

## 12. Possíveis evoluções

Se a atividade pedir um próximo passo, as evoluções naturais seriam:

- trocar `Map` por classes DTO com tipagem forte;
- adicionar histórico de execuções do pipeline;
- transformar os status em enum;
- expor métricas com Actuator;
- separar mais camadas com interfaces;
- escrever testes de integração com `MockMvc`.

Nada disso é necessário agora: a versão atual prioriza clareza e baixo acoplamento.
