package resourcebooking.demo;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    // ESTE ES EL WORKER: Escucha mensajes en segundo plano
    @RabbitListener(queues = "emails")
    public void receiveMessage(String message) {
        System.out.println("========================================");
        System.out.println("⚡ WORKER: Recibido encargo -> " + message);
        System.out.println("📧 Enviando email de confirmación...");
        System.out.println("========================================");
    }
}