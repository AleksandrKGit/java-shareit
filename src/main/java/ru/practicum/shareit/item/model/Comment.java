package ru.practicum.shareit.item.model;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.user.User;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Getter
@Setter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(length = 2000)
    String text;

    @ManyToOne
    @JoinColumn(nullable = false, updatable = false)
    Item item;

    @ManyToOne
    @JoinColumn(nullable = false, updatable = false)
    User author;

    LocalDateTime created;
}