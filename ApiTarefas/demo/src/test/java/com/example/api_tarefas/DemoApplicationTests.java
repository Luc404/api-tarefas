// Pacote igual ao da aplicação para o Spring encontrar a configuração.
package com.example.api_tarefas;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// Sobe o contexto Spring só para conferir se nada está quebrado.
@SpringBootTest
class DemoApplicationTests {

	// Sem asserção: se a aplicação carregar, o teste passa.
	@Test
	void contextLoads() {
	}

}
