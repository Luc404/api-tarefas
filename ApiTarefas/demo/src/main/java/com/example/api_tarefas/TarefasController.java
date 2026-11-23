/*
 * Controller da API de tarefas.
 * Ele transforma chamadas HTTP em respostas JSON.
 */
// Mesmo pacote da classe principal para o @ComponentScan encontrar esta classe.
package com.example.api_tarefas;

// List e Map montam as respostas sem precisar criar classes extras.
import java.util.List;
import java.util.Map;

// @GetMapping define a rota; @RestController transforma retornos em JSON.
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/*
 * @RestController já inclui @Controller e @ResponseBody.
 * Isso significa que os métodos retornam dados diretamente no corpo da resposta,
 * e não o nome de uma página HTML.
 */
@RestController
public class TarefasController {

    /*
     * Rota GET /.
     * Map.of cria um mapa imutável com duas chaves: status e aplicacao.
     * O Spring converte esse mapa em {"status":"ok","aplicacao":"api-tarefas"}.
     */
    @GetMapping("/")
    public Map<String, String> status(){
        return Map.of(
            "status", "ok", "aplicacao", "api-tarefas"
        );
    }

    /*
     * Rota GET /tarefas.
     * O retorno é uma lista de mapas: cada mapa representa uma tarefa.
     * Map<String, Object> é usado porque os valores misturam tipos diferentes:
     * id é número (Integer), titulo é texto (String) e concluida é booleano.
     * List.of também cria uma lista imutável, ideal para dados fixos de exemplo.
     */
    @GetMapping("/tarefas")
    public List<Map<String, Object>> ListarTarefas(){
        // Cada bloco Map.of vira um objeto JSON dentro do array da resposta.
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
