package ru.practicum.shareit.user;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.support.DefaultLocaleMessageSource;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.validation.groups.OnCreate;
import ru.practicum.shareit.validation.groups.OnUpdate;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/users")
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class UserController {
    DefaultLocaleMessageSource messageSource;

    UserService userService;

    @PostMapping
    public ResponseEntity<UserDto> create(@Validated(OnCreate.class) @RequestBody UserDto userDto) {
        userDto = userService.create(userDto);
        log.info("{}: {}", messageSource.get("user.UserController.create"), userDto);
        return ResponseEntity.ok(userDto);
    }

    @GetMapping
    public ResponseEntity<Set<UserDto>> readAll() {
        Set<UserDto> usersDto = userService.readAll();
        log.info("{}: {}", messageSource.get("user.UserController.readAll"),
                usersDto.stream().map(UserDto::getId).collect(Collectors.toSet()));
        return ResponseEntity.ok(usersDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> readById(@PathVariable Long id) {
        UserDto userDto = userService.readById(id);
        log.info("{}: {}", messageSource.get("user.UserController.readById"), userDto);
        return ResponseEntity.ok(userDto);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserDto> update(@PathVariable Long id,
                                          @Validated(OnUpdate.class) @RequestBody UserDto userDto) {
        userDto = userService.update(id, userDto);
        log.info("{}: {}", messageSource.get("user.UserController.update"), userDto);
        return ResponseEntity.ok(userDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        userService.delete(id);
        log.info("{}: {}", messageSource.get("user.UserController.delete"), id);
        return ResponseEntity.ok("");
    }
}