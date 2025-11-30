package resourcebooking.demo;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {


    @RabbitListener(queues = "emails")
    public void receiveMessage(String message) {
        System.out.println("Worker :received " + message);
        System.out.println("Sending email confirmation...");
    }
}