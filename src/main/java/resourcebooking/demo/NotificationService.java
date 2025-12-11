package resourcebooking.demo;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("worker")   // <-- SOLO se carga cuando el perfil 'worker' está activo
public class NotificationService {

    @RabbitListener(queues = "emails")
    public void receiveMessage(String message) {
        System.out.println("Worker :received " + message);
        System.out.println("Sending email confirmation...");
    }
}
