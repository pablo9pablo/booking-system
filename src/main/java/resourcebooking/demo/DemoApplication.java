package resourcebooking.demo;

import org.springframework.amqp.core.Queue; // <--- IMPORT NUEVO
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean; // <--- IMPORT NUEVO

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	// ESTO CREA EL BUZÓN AUTOMÁTICAMENTE
	@Bean
	public Queue myQueue() {
		return new Queue("emails", false);
	}
}
