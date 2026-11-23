// Pacote usado pelo Spring para encontrar as classes da aplicação.
package com.example.api_tarefas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Mesmo pacote do controller para o scan do Spring encontrar tudo.
@SpringBootApplication
public class DemoApplication {

	// Ponto de entrada: inicia o servidor web da API.
	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

}
