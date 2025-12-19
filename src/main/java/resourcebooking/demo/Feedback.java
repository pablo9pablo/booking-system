package resourcebooking.demo;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "feedbacks")
public class Feedback implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer rating; // Ej: 1 a 5
    private String comment; // Ej: "Todo perfecto"

    private LocalDateTime createdAt;

    // RELACIÓN: Este campo crea la columna "booking_id" en la base de datos.
    // Vincula este feedback a una reserva concreta.
    @OneToOne
    @JoinColumn(name = "booking_id", referencedColumnName = "id")
    private Booking booking;

    // CONSTRUCTORES
    public Feedback() {
        this.createdAt = LocalDateTime.now();
    }

    public Feedback(Integer rating, String comment, Booking booking) {
        this.rating = rating;
        this.comment = comment;
        this.booking = booking;
        this.createdAt = LocalDateTime.now();
    }

    // GETTERS Y SETTERS
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }
}