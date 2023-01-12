package ru.practicum.shareit.user;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.support.DefaultLocaleMessageSource;
import javax.validation.Valid;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/users")
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {
    DefaultLocaleMessageSource messageSource;
    UserService userService;

    @Autowired
    public UserController(DefaultLocaleMessageSource messageSource, UserService userService) {
        this.messageSource = messageSource;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserDto> create(@Valid @RequestBody UserDto userDto) {
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
    public ResponseEntity<UserDto> readById(@PathVariable Integer id) {
        UserDto userDto = userService.readById(id);
        log.info("{}: {}", messageSource.get("user.UserController.readById"), userDto);
        return ResponseEntity.ok(userDto);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserDto> update(@PathVariable Integer id,
                                          @Valid @RequestBody UserDto userDto) {
        userDto.setId(id);
        userDto = userService.update(userDto);
        log.info("{}: {}", messageSource.get("user.UserController.update"), userDto);
        return ResponseEntity.ok(userDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Integer id) {
        userService.delete(id);
        log.info("{}: {}", messageSource.get("user.UserController.delete"), id);
        return ResponseEntity.ok("");
    }
}