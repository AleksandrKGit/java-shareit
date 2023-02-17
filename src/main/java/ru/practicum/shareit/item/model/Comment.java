package ru.practicum.shareit.item.model;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.user.User;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "comments")
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, length = 2000)
    String text;

    @ManyToOne
    @JoinColumn(nullable = false, updatable = false)
    Item item;

    @ManyToOne
    @JoinColumn(nullable = false, updatable = false)
    User author;

    @Column
    LocalDateTime created;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Comment)) {
            return false;
        }

        return id != null && id.equals(((Comment) obj).getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}