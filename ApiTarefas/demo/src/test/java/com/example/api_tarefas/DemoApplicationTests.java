/*
 * Teste de inicialização da aplicação.
 * Ele responde à pergunta: "o contexto Spring consegue carregar sem erro?".
 */
// Pacote igual ao da aplicação para o @SpringBootTest achar a configuração.
package com.example.api_tarefas;

// @Test marca o método; @SpringBootTest carrega o contexto da aplicação.
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/*
 * @SpringBootTest procura a classe @SpringBootApplication e monta o contexto.
 * Ele usa um ambiente web simulado: não abre a porta 8080 de verdade.
 * Se algum bean estiver quebrado (controller, serviço ou configuração), o teste falha.
 */
@SpringBootTest
class DemoApplicationTests {

    /*
     * Não há assert porque o objetivo é só verificar se a inicialização funciona.
     * Se o método termina sem exceção, o JUnit considera o teste aprovado.
     */
    @Test
    void contextLoads() {
    }

}
