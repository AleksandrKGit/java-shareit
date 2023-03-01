package ru.practicum.shareit.user;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.support.DefaultLocaleMessageSource;
import ru.practicum.shareit.validation.groups.OnCreate;
import ru.practicum.shareit.validation.groups.OnUpdate;

@Slf4j
@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {
    DefaultLocaleMessageSource messageSource;

    UserClient client;

    @PostMapping
    public ResponseEntity<Object> create(@Validated(OnCreate.class) @RequestBody UserDtoFromClient inDto) {
        log.info("{}: {}", messageSource.get("user.UserController.create"), inDto);
        return client.create(inDto);
    }

    @GetMapping
    public ResponseEntity<Object> readAll() {
        log.info("{}", messageSource.get("user.UserController.readAll"));
        return client.readAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> readById(@PathVariable Long id) {
        log.info("{}: {}", messageSource.get("user.UserController.readById"), id);
        return client.readById(id);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> update(@PathVariable Long id,
                                         @Validated(OnUpdate.class) @RequestBody UserDtoFromClient inDto) {
        log.info("{}: {}, {}", messageSource.get("user.UserController.update"), id, inDto);
        return client.update(id, inDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> delete(@PathVariable Long id) {
        log.info("{}: {}", messageSource.get("user.UserController.delete"), id);
        return client.deleteById(id);
    }
}