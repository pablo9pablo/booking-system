package resourcebooking.demo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
public class BookingController {

    @Autowired
    private BookingRepository bookingRepository;

    // Endpoint para ver todas las reservas (Service #15 - Realtime Availability)
    @GetMapping("/bookings")
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    // Endpoint para crear una reserva con LOGICA DE NEGOCIO (Service #14)
    @PostMapping("/book")
    public String createBooking(@RequestBody Booking newBooking) {
        // 1. Validar conflictos (Lógica de concurrencia)
        List<Booking> conflicts = bookingRepository.findConflictingBookings(
                newBooking.getResourceName(),
                newBooking.getStartTime(),
                newBooking.getEndTime()
        );

        if (!conflicts.isEmpty()) {
            return "ERROR: Resource is already booked for this time slot!";
        }

        // 2. Guardar si está libre
        bookingRepository.save(newBooking);
        return "SUCCESS: Booking confirmed for " + newBooking.getResourceName();
    }
}