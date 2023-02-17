package ru.practicum.shareit.user;

import javax.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.Objects;

@Entity
@Table(name = "users")
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    String name;

    @Column(nullable = false, unique = true)
    String email;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof User)) {
            return false;
        }

        return id != null && id.equals(((User) obj).getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}