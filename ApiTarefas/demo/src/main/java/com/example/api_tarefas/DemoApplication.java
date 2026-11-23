/*
 * Classe principal da aplicação Spring Boot.
 * Ela é o ponto de partida usado pelo JAR executável e pelo Maven.
 */
// O pacote define onde o Spring vai procurar componentes automaticamente.
package com.example.api_tarefas;

// SpringApplication inicia a aplicação; @SpringBootApplication configura o Spring.
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*
 * @SpringBootApplication é uma anotação combinada:
 * - @Configuration: marca esta classe como fonte de configuração;
 * - @EnableAutoConfiguration: configura servidor web, JSON e outros recursos;
 * - @ComponentScan: encontra controllers e serviços neste pacote e subpacotes.
 * Graças a isso, TarefasController e AwsSimulationService são descobertos sozinhos.
 */
@SpringBootApplication
public class DemoApplication {

    /*
     * Método chamado quando o Java executa o JAR.
     * SpringApplication.run cria o contexto, sobe o servidor embutido na porta 8080
     * e mantém o processo vivo até receber um sinal de encerramento.
     */
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

}
