/*
 * Serviço da camada de simulação.
 * Ele guarda em memória o estado dos "recursos" que parecem serviços AWS,
 * mas não faz nenhuma chamada externa nem cria nada fora da aplicação.
 */
// Subpacote de api_tarefas: continua sendo encontrado pelo @ComponentScan.
package com.example.api_tarefas.aws;

// LinkedHashMap mantém a ordem de inserção, deixando o JSON previsível.
// List guarda a lista fixa de papéis; Map monta respostas no formato JSON.
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// @Service faz o Spring criar um único objeto deste serviço e injetá-lo no controller.
import org.springframework.stereotype.Service;

/*
 * Todo o estado vive nestes campos enquanto a aplicação está no ar.
 * Quando a aplicação para, os valores voltam ao inicial: não há banco nem disco.
 */
@Service
public class AwsSimulationService {

    // Simula o bucket S3: nome do artefato -> descrição do artefato.
    private final Map<String, String> artefatos = new LinkedHashMap<>();

    // Estados iniciais, como se o pipeline ainda não tivesse sido disparado.
    private String statusPipeline = "PRONTO";
    private String statusBuild = "NAO_EXECUTADO";
    private String statusDeploy = "NAO_EXECUTADO";

    /*
     * Executa o fluxo simulado em ordem:
     * 1. pipeline entra em EXECUTANDO;
     * 2. build termina com SUCESSO;
     * 3. os artefatos aparecem no "S3" em memória;
     * 4. deploy termina com SUCESSO;
     * 5. pipeline é marcado como CONCLUIDO.
     * Depois devolve o estado completo para quem chamou a rota.
     */
    public Map<String, Object> executarPipeline() {
        statusPipeline = "EXECUTANDO";
        statusBuild = "SUCESSO";
        artefatos.put("app.jar", "JAR simulado gerado pelo build");
        artefatos.put("appspec.yml", "AppSpec simulado");
        statusDeploy = "SUCESSO";
        statusPipeline = "CONCLUIDO";
        return status();
    }

    /*
     * Monta a resposta de status como um Map<String, Object>.
     * Object é necessário porque dentro do mapa há textos, mapas e listas.
     * Cada mapa interno representa um recurso simulado com nome e estado.
     */
    public Map<String, Object> status() {
        Map<String, Object> resposta = new LinkedHashMap<>();
        resposta.put("ambiente", "SIMULADO");

        // CodePipeline: mostra o nome e o estado atual do fluxo.
        Map<String, Object> pipeline = new LinkedHashMap<>();
        pipeline.put("nome", "api-tarefas-pipeline");
        pipeline.put("status", statusPipeline);
        resposta.put("codepipeline", pipeline);

        // CodeBuild: mostra se o build simulado já aconteceu.
        Map<String, Object> build = new LinkedHashMap<>();
        build.put("nome", "api-tarefas-build");
        build.put("status", statusBuild);
        resposta.put("codebuild", build);

        // S3: bucket fictício e artefatos guardados no mapa artefatos.
        Map<String, Object> s3 = new LinkedHashMap<>();
        s3.put("bucket", "api-tarefas-artefatos-simulado");
        s3.put("artefatos", artefatos);
        resposta.put("s3", s3);

        // CodeDeploy: aplicação e grupo simulados, com o estado do deploy.
        Map<String, Object> deploy = new LinkedHashMap<>();
        deploy.put("aplicacao", "api-tarefas");
        deploy.put("grupo", "api-tarefas-dg");
        deploy.put("status", statusDeploy);
        resposta.put("codedeploy", deploy);

        // EC2: o estado é calculado a partir do deploy; não existe instância real.
        Map<String, Object> ec2 = new LinkedHashMap<>();
        ec2.put("nome", "api-tarefas");
        ec2.put("tipo", "t3.micro");
        ec2.put("estado", statusDeploy.equals("SUCESSO") ? "RODANDO (simulado)" : "PARADO (simulado)");
        resposta.put("ec2", ec2);

        // IAM: apenas uma lista ilustrativa com os nomes dos papéis do fluxo.
        Map<String, Object> iam = new LinkedHashMap<>();
        iam.put("papeis", List.of(
            "api-tarefas-codebuild-role",
            "api-tarefas-pipeline-role",
            "api-tarefas-codedeploy-role",
            "api-tarefas-ec2-role"
        ));
        resposta.put("iam", iam);

        return resposta;
    }
}
