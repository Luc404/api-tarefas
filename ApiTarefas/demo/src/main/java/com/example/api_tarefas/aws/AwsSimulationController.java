/*
 * Controller da simulação.
 * É a "porta de entrada" HTTP para consultar e executar o fluxo simulado.
 */
// Subpacote de api_tarefas para continuar sob o @ComponentScan da classe principal.
package com.example.api_tarefas.aws;

// Map é usado como corpo da resposta JSON, igual nos outros controllers.
import java.util.Map;

// Anotações do Spring MVC para mapear URLs e definir o tipo de retorno.
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*
 * @RestController devolve JSON diretamente.
 * @RequestMapping("/aws") cria o prefixo comum de todas as rotas desta classe.
 * Portanto /status vira /aws/status e /pipeline/run vira /aws/pipeline/run.
 */
@RestController
@RequestMapping("/aws")
public class AwsSimulationController {

    // Dependência do serviço que realmente guarda o estado simulado.
    private final AwsSimulationService servico;

    /*
     * O Spring cria o controller e passa o serviço automaticamente (injeção).
     * O campo é final para garantir que a dependência nunca seja trocada.
     */
    public AwsSimulationController(AwsSimulationService servico) {
        this.servico = servico;
    }

    /*
     * GET /aws/status: só lê o estado atual, sem mudar nada.
     * É útil para verificar o estado inicial ou conferir o resultado do pipeline.
     */
    @GetMapping("/status")
    public Map<String, Object> status() {
        return servico.status();
    }

    /*
     * POST /aws/pipeline/run: dispara a simulação.
     * POST é usado porque a chamada altera o estado em memória do serviço.
     */
    @PostMapping("/pipeline/run")
    public Map<String, Object> executar() {
        return servico.executarPipeline();
    }
}
