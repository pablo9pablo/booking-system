package resourcebooking.demo;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired; // IMPORTANTE
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("worker") // Solo funciona en el dyno 'worker'
public class NotificationService {

    // 1. INYECTAMOS TU SERVICIO DE EMAIL (MAILTRAP)
    @Autowired
    private EmailService emailService;

    @RabbitListener(queues = "emails")
    public void receiveMessage(String message) {
        System.out.println("👷 WORKER: Mensaje recibido de la cola: " + message);

        // 2. LLAMAMOS AL SERVICIO PARA QUE ENVÍE EL CORREO DE VERDAD
        System.out.println("📨 WORKER: Enviando email a través de EmailService...");
        emailService.sendEmail(message);
    }
}