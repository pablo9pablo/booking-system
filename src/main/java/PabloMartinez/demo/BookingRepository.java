package PabloMartinez.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Esta es la MAGIA: Busca si hay alguna reserva que choque con las horas que pides.
    // Esto evita el "Double Booking" (Service #14).
    @Query("SELECT b FROM Booking b WHERE b.resourceName = :resource " +
            "AND ((b.startTime < :end AND b.endTime > :start))")
    List<Booking> findConflictingBookings(@Param("resource") String resource,
                                          @Param("start") LocalDateTime start,
                                          @Param("end") LocalDateTime end);
}