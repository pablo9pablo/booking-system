package resourcebooking.demo;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonIgnore; // Importante para evitar errores de bucle

@Entity
@Table(name = "bookings")
public class Booking implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String resourceName;
    private String userEmail;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // RELACIÓN NUEVA: Una reserva puede tener un Feedback asociado.
    // "mappedBy" indica que la clave foránea está en la otra clase (Feedback).
    // CascadeType.ALL permite que si borras la reserva, se borre su feedback.
    @OneToOne(mappedBy = "booking", cascade = CascadeType.ALL)
    @JsonIgnore // Evita que al pedir la reserva, intente pintar el feedback infinitamente
    private Feedback feedback;

    // --- GETTERS Y SETTERS ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getResourceName() { return resourceName; }
    public void setResourceName(String resourceName) { this.resourceName = resourceName; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public Feedback getFeedback() { return feedback; }
    public void setFeedback(Feedback feedback) { this.feedback = feedback; }
}