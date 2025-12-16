package resourcebooking.demo;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.List;
import org.springframework.data.redis.core.StringRedisTemplate;


@RestController
@RequestMapping("/api")
public class BookingController {
    @Autowired
    private org.springframework.data.redis.core.StringRedisTemplate redisTemplate;
    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate; // para enviar mensajes

    @GetMapping("/redis-test")
    public String testRedisConnection() {
        try {
            // 1. Intenta escribir en Redis
            redisTemplate.opsForValue().set("prueba_conexion", "¡Hola Redis desde Heroku!");

            // 2. Intenta leer de Redis
            String valor = redisTemplate.opsForValue().get("prueba_conexion");

            // 3. Si llega aquí, todo funciona
            return "ÉXITO: Redis responde correctamente. Valor recuperado: " + valor;
        } catch (Exception e) {
            // 4. Si falla, nos dirá el error exacto
            return "ERROR GRAVE: No se pudo conectar a Redis. Causa: " + e.getMessage();
        }
    }
    // Para ver el estado actual de las reservas
    @Cacheable("bookings_v2")
    @GetMapping("/bookings")
    public List<Booking> getAllBookings() {
        System.out.println("--- ⚠️ LLAMANDO A LA BASE DE DATOS (NO CACHÉ) ⚠️ ---");
        if(bookingRepository.findAll().isEmpty()){

            System.out.println("No bookings found");
        }
        return bookingRepository.findAll();
    }

    // gestiona conflictos y crea la reserva
    @CacheEvict(value = "bookings_v2", allEntries = true)
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
        bookingRepository.save(newBooking);

        String mensaje = "New confirmed reserve for : " + newBooking.getUserEmail();
        rabbitTemplate.convertAndSend("emails", mensaje);

        return "Succesfull reserve";
    }
    @CacheEvict(value = "bookings_v2", allEntries = true)
    @DeleteMapping("/book/{id}")
    public String cancelBooking(@PathVariable Long id) {
        if (!bookingRepository.existsById(id)) {
            return "ERROR: Reserve doesn't exist with ID: " + id;
        }
        bookingRepository.deleteById(id);
        return "Reserve" + id + " succesfully cancelled.";
    }

    @CacheEvict(value = "bookings_v2", allEntries = true)
    @DeleteMapping("/bookings")
    public String deleteAllBookings() {
        bookingRepository.deleteAll();
        return "All bookings deletedd";
    }

    @GetMapping("/stats")
    public Map<String, Object> getStats() {

        Map<String, Object> stats = new HashMap<>();

        stats.put("totalBookings", bookingRepository.count());

        Booking last = bookingRepository.findTopByOrderByEndTimeDesc();
        if (last != null) {
            stats.put("lastBookingResource", last.getResourceName());
            stats.put("lastBookingEndTime", last.getEndTime());
        }

        Map<String, Long> bookingsPerResource = new HashMap<>();
        for (Object[] row : bookingRepository.countBookingsByResource()) {
            String resource = (String) row[0];
            Long count = (Long) row[1];
            bookingsPerResource.put(resource, count);
        }

        stats.put("bookingsPerResource", bookingsPerResource);

        return stats;
    }
    @GetMapping("/bookings/byUser")
    public List<Booking> getBookingsByUser(@RequestParam String userEmail) {
        return bookingRepository.findByUserEmail(userEmail);
    }

    @GetMapping("/bookings/byResource")
    public List<Booking> getBookingsByResource(@RequestParam String resourceName) {
        return bookingRepository.findByResourceName(resourceName);
    }

    @GetMapping("/bookings/byDay")
    public List<Booking> getBookingsByDay(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        return bookingRepository.findByDay(startOfDay, endOfDay);
    }

}