/*
 * Teste unitário da simulação.
 * Ele não sobe o servidor: cria o serviço diretamente com new.
 */
// Mesmo pacote do serviço testado para acessar a classe sem import extra.
package com.example.api_tarefas.aws;

// Asserts do JUnit: comparam o esperado com o obtido.
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Map é o tipo usado pelo serviço para devolver respostas em formato JSON.
import java.util.Map;

// @Test marca o método como teste executável pelo Maven.
import org.junit.jupiter.api.Test;

class AwsSimulationServiceTest {

    /*
     * Verifica duas coisas:
     * 1. depois de executar o pipeline, o status do CodePipeline é CONCLUIDO;
     * 2. o "S3" simulado passa a conter o artefato app.jar.
     */
    @Test
    // O cast de Map<String, Object> para mapas internos gera aviso de unchecked.
    @SuppressWarnings("unchecked")
    void pipelineSimuladoTerminaComSucesso() {
        AwsSimulationService servico = new AwsSimulationService();
        Map<String, Object> resultado = servico.executarPipeline();

        // Pega o mapa do pipeline dentro da resposta e confere o status final.
        Map<String, Object> pipeline = (Map<String, Object>) resultado.get("codepipeline");
        assertEquals("CONCLUIDO", pipeline.get("status"));

        // Pega o mapa do S3, depois o mapa de artefatos, e procura app.jar.
        Map<String, Object> s3 = (Map<String, Object>) resultado.get("s3");
        Map<String, Object> artefatos = (Map<String, Object>) s3.get("artefatos");
        assertTrue(artefatos.containsKey("app.jar"));
    }
}
