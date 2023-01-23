package ru.practicum.shareit.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemRequest {
    Integer id;
    String description;
    User requestor;
    LocalDateTime created;
}
