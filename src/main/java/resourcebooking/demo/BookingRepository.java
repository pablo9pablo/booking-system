package resourcebooking.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    //evita el double Booking
    @Query("SELECT b FROM Booking b WHERE b.resourceName = :resource " +
            "AND b.startTime < :end AND b.endTime > :start")
    List<Booking> findConflictingBookings(@Param("resource") String resource,
                                          @Param("start") LocalDateTime start,
                                          @Param("end") LocalDateTime end);


    List<Booking> findByUserEmail(String userEmail);
    List<Booking> findByUserEmailAndResourceName(String userEmail, String resourceName);
}