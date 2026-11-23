// Rotas da simulação dos recursos AWS.
package com.example.api_tarefas.aws;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Expõe o estado simulado como se fosse uma API de console da AWS.
@RestController
@RequestMapping("/aws")
public class AwsSimulationController {

    private final AwsSimulationService servico;

    // Recebe o serviço injetado pelo Spring.
    public AwsSimulationController(AwsSimulationService servico) {
        this.servico = servico;
    }

    // Mostra o estado atual da simulação.
    @GetMapping("/status")
    public Map<String, Object> status() {
        return servico.status();
    }

    // Executa o pipeline simulado de ponta a ponta.
    @PostMapping("/pipeline/run")
    public Map<String, Object> executar() {
        return servico.executarPipeline();
    }
}
