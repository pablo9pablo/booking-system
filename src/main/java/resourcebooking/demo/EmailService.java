package resourcebooking.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender emailSender;

    public void sendEmail(String mensaje) {
        try {
            System.out.println("📤 Intentando enviar email vía Mailtrap...");

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@tuproyecto.com");
            message.setTo("profesor@test.com"); // Da igual, Mailtrap lo captura todo
            message.setSubject("Prueba de Servicio SaaS - Mailtrap");
            message.setText("Este es un mensaje automático desde Heroku:\n\n" + mensaje);

            emailSender.send(message);

            System.out.println("✅ Email enviado correctamente a la bandeja de Mailtrap.");
        } catch (Exception e) {
            System.err.println("❌ Error enviando email: " + e.getMessage());
            e.printStackTrace();
        }
    }
}