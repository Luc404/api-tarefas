// Teste do serviço simulado, sem precisar subir a aplicação.
package com.example.api_tarefas.aws;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

class AwsSimulationServiceTest {

    @Test
    @SuppressWarnings("unchecked")
    void pipelineSimuladoTerminaComSucesso() {
        AwsSimulationService servico = new AwsSimulationService();
        Map<String, Object> resultado = servico.executarPipeline();

        Map<String, Object> pipeline = (Map<String, Object>) resultado.get("codepipeline");
        assertEquals("CONCLUIDO", pipeline.get("status"));

        Map<String, Object> s3 = (Map<String, Object>) resultado.get("s3");
        Map<String, Object> artefatos = (Map<String, Object>) s3.get("artefatos");
        assertTrue(artefatos.containsKey("app.jar"));
    }
}
