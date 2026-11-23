// Serviço que finge recursos AWS sem chamar a AWS de verdade.
package com.example.api_tarefas.aws;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

// Guarda o estado simulado em memória enquanto a aplicação está rodando.
@Service
public class AwsSimulationService {

    private final Map<String, String> artefatos = new LinkedHashMap<>();
    private String statusPipeline = "PRONTO";
    private String statusBuild = "NAO_EXECUTADO";
    private String statusDeploy = "NAO_EXECUTADO";

    // Executa as etapas simuladas e guarda o resultado.
    public Map<String, Object> executarPipeline() {
        statusPipeline = "EXECUTANDO";
        statusBuild = "SUCESSO";
        artefatos.put("app.jar", "JAR simulado gerado pelo build");
        artefatos.put("appspec.yml", "AppSpec simulado");
        statusDeploy = "SUCESSO";
        statusPipeline = "CONCLUIDO";
        return status();
    }

    // Devolve o estado atual de todos os recursos simulados.
    public Map<String, Object> status() {
        Map<String, Object> resposta = new LinkedHashMap<>();
        resposta.put("ambiente", "SIMULADO");

        Map<String, Object> pipeline = new LinkedHashMap<>();
        pipeline.put("nome", "api-tarefas-pipeline");
        pipeline.put("status", statusPipeline);
        resposta.put("codepipeline", pipeline);

        Map<String, Object> build = new LinkedHashMap<>();
        build.put("nome", "api-tarefas-build");
        build.put("status", statusBuild);
        resposta.put("codebuild", build);

        Map<String, Object> s3 = new LinkedHashMap<>();
        s3.put("bucket", "api-tarefas-artefatos-simulado");
        s3.put("artefatos", artefatos);
        resposta.put("s3", s3);

        Map<String, Object> deploy = new LinkedHashMap<>();
        deploy.put("aplicacao", "api-tarefas");
        deploy.put("grupo", "api-tarefas-dg");
        deploy.put("status", statusDeploy);
        resposta.put("codedeploy", deploy);

        Map<String, Object> ec2 = new LinkedHashMap<>();
        ec2.put("nome", "api-tarefas");
        ec2.put("tipo", "t3.micro");
        ec2.put("estado", statusDeploy.equals("SUCESSO") ? "RODANDO (simulado)" : "PARADO (simulado)");
        resposta.put("ec2", ec2);

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
