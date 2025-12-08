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
    public Object getAllBookings() {
        List<Booking> lista = bookingRepository.findAll();
        if (lista.isEmpty()) {
            return "Booking list is empty";
        }

        return lista;
    }

    // gestiona conflictos y crea la reserva
    @PostMapping("/book")
    public String createBooking(@RequestBody Booking newBooking) {
        // Mira si la sala esta libre
        if (newBooking.getEndTime().isBefore(newBooking.getStartTime())) {
            return "ERROR: End date is before start date.";
        }

        List<Booking> conflicts = bookingRepository.findConflictingBookings(
                newBooking.getResourceName(),
                newBooking.getStartTime(),
                newBooking.getEndTime()
        );

        if (!conflicts.isEmpty()) {
            return "ERROR: The room is reserved for that time";
        }

        bookingRepository.save(newBooking);
        String mensaje = "NEw booking confirmed for: " + newBooking.getUserEmail();
        rabbitTemplate.convertAndSend("emails", mensaje);

        return "Succesfull reserve";
    }

    @DeleteMapping("/book/{id}")
    public String cancelBooking(@PathVariable Long id) {
        if (!bookingRepository.existsById(id)) {
            return "ERROR: Reserve doesn't exist with ID: " + id;
        }
        bookingRepository.deleteById(id);
        return "Reserve" + id + " succesfully cancelled.";
    }

    @DeleteMapping("/bookings")
    public String deleteAllBookings() {
        bookingRepository.deleteAll();
        return "All reserves were deleted.";
    }
}