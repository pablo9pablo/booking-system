package resourcebooking.demo;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class BookingController {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate; // El cartero de RabbitMQ

    // 1. Ver todas las reservas (GET)
    // Esto cumple el servicio "Realtime Resource Availability"
    @GetMapping("/bookings")
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    // 2. Crear reserva (POST)
    // Esto cumple "Resource Reservation" y activa el "Messaging Service"
    @PostMapping("/book")
    public String createBooking(@RequestBody Booking newBooking) {

        // A. Lógica de Negocio: Evitar conflictos (Locking)
        // Busca si ya hay alguien en esa sala a esa hora
        List<Booking> conflicts = bookingRepository.findConflictingBookings(
                newBooking.getResourceName(),
                newBooking.getStartTime(),
                newBooking.getEndTime()
        );

        if (!conflicts.isEmpty()) {
            return "❌ ERROR: ¡Esa sala ya está ocupada en ese horario!";
        }

        // B. Guardar en Base de Datos (DBaaS)
        bookingRepository.save(newBooking);

        // C. Enviar mensaje a la cola (Messaging Service)
        // El Worker escuchará esto y "enviará el email"
        String mensaje = "Nueva reserva confirmada para: " + newBooking.getUserEmail();
        rabbitTemplate.convertAndSend("emails", mensaje);

        return "✅ ÉXITO: Reserva guardada y notificación enviada a la cola.";
    }
}