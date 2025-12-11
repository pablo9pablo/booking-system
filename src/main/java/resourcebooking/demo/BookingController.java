package resourcebooking.demo;
import org.springframework.transaction.annotation.Transactional;

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
        if(bookingRepository.findAll().isEmpty()){
            System.out.println("No bookings found");
        }
        return bookingRepository.findAll();
    }

    // gestiona conflictos y crea la reserva
    @Transactional
    @PostMapping("/book")
    public String createBooking(@RequestBody Booking newBooking) {

        if (newBooking.getEndTime().isBefore(newBooking.getStartTime())) {
            return "ERROR: End date cannot be before start date.";
        }

        List<Booking> conflicts = bookingRepository.findConflictingBookings(
                newBooking.getResourceName(),
                newBooking.getStartTime(),
                newBooking.getEndTime()
        );

        if (!conflicts.isEmpty()) {
            return "ERROR: The room is already reserved for that time.";
        }

        // 3. GUARDAR
        bookingRepository.save(newBooking);

        String mensaje = "Nueva reserva confirmada para: " + newBooking.getUserEmail();
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
        return "All bookings deletedd";
    }
}