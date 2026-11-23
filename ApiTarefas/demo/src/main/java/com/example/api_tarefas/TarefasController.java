// Pacote da aplicação: mantém o controller junto com a classe principal.
package com.example.api_tarefas;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// Transforma esta classe em uma API REST descoberta pelo Spring.
@RestController
public class TarefasController {

    // Rota inicial: devolve um JSON simples de status.
    @GetMapping("/")
    public Map<String, String> status(){
        return Map.of(
            "status", "ok", "aplicacao", "api-tarefas"
        );
    }

    // Rota de tarefas: devolve uma lista fixa de tarefas.
    @GetMapping("/tarefas")
    public List<Map<String, Object>> ListarTarefas(){
        // Cada Map vira um objeto dentro da resposta JSON.
        return List.of(
          Map.of(
            "id", 1, "titulo", "Estudar Java", "concluida", false
          ),
          Map.of(
                "id", 2,
                "titulo", "Aprender AWS",
                "concluida", false
            )
        );
    }

}
