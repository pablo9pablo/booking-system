package resourcebooking.demo;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.springframework.format.annotation.DateTimeFormat;

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

        String mensaje = "New confirmed reserve for : " + newBooking.getUserEmail();
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

    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        // Mapa principal de respuesta
        Map<String, Object> stats = new HashMap<>();

        // 1. Total de reservas
        long total = bookingRepository.count();
        stats.put("totalBookings", total);

        // 2. Última reserva (por fecha de fin)
        Booking last = bookingRepository.findTopByOrderByEndTimeDesc();
        if (last != null) {
            stats.put("lastBookingResource", last.getResourceName());
            stats.put("lastBookingEndTime", last.getEndTime());
        }

        // 3. Número de reservas por recurso
        Map<String, Long> perResource = new HashMap<>();
        for (Booking b : bookingRepository.findAll()) {
            String resource = b.getResourceName();
            perResource.put(resource, perResource.getOrDefault(resource, 0L) + 1);
        }
        stats.put("bookingsPerResource", perResource);

        return stats;
    }


    // /api/bookings/byUser?userEmail=vip@test.com
    @GetMapping("/bookings/byUser")
    public List<Booking> getBookingsByUser(@RequestParam String userEmail) {
        return bookingRepository.findByUserEmail(userEmail);
    }

    // /api/bookings/byResource?resourceName=Sala%20VIPP
    @GetMapping("/bookings/byResource")
    public List<Booking> getBookingsByResource(@RequestParam String resourceName) {
        return bookingRepository.findByResourceName(resourceName);
    }

    // /api/bookings/byDay?date=2025-12-11
    @GetMapping("/bookings/byDay")
    public List<Booking> getBookingsByDay(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        return bookingRepository.findByDay(startOfDay, endOfDay);
    }

}