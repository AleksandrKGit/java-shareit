package ru.practicum.shareit.item.model;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;
import javax.persistence.*;

@Entity
@Table(name = "items")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    String name;

    @Column(length = 2000)
    String description;

    @Column(name = "is_available")
    Boolean available;

    @ManyToOne
    @JoinColumn(nullable = false, updatable = false)
    User owner;

    @ManyToOne
    @JoinColumn(updatable = false)
    ItemRequest request;
}