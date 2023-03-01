package ru.practicum.shareit.booking.model;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, name = "start_date")
    LocalDateTime start;

    @Column(nullable = false, name = "end_date")
    LocalDateTime end;

    @ManyToOne
    @JoinColumn(nullable = false, updatable = false)
    Item item;

    @ManyToOne
    @JoinColumn(nullable = false, updatable = false)
    User booker;

    @Enumerated(EnumType.STRING)
    BookingStatus status;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Booking)) {
            return false;
        }

        return id != null && id.equals(((Booking) obj).getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
