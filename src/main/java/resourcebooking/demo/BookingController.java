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
    private RabbitTemplate rabbitTemplate; // para enviar mensajes

    // Para ver el estado actual de las reservas
    @GetMapping("/bookings")
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    // gestiona conflictos y crea la reserva
    @PostMapping("/book")
    public String createBooking(@RequestBody Booking newBooking) {
        // Mira si la sala esta libre
        List<Booking> conflicts = bookingRepository.findConflictingBookings(
                newBooking.getResourceName(),
                newBooking.getStartTime(),
                newBooking.getEndTime()
        );

        if (!conflicts.isEmpty()) {
            return "ERROR: The room is reserved for that time";
        }

        bookingRepository.save(newBooking);

        String mensaje = "Nueva reserva confirmada para: " + newBooking.getUserEmail();
        rabbitTemplate.convertAndSend("emails", mensaje);

        return "Succesfull reserve";
    }
}