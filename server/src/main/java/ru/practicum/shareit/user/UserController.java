package ru.practicum.shareit.user;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.support.DefaultLocaleMessageSource;
import ru.practicum.shareit.user.dto.UserDtoFromClient;
import ru.practicum.shareit.user.dto.UserDtoToClient;
import ru.practicum.shareit.user.service.UserService;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {
    DefaultLocaleMessageSource messageSource;

    UserService service;

    @PostMapping
    public ResponseEntity<UserDtoToClient> create(@RequestBody UserDtoFromClient inDto) {
        UserDtoToClient outDto = service.create(inDto);
        log.info("{}: {}", messageSource.get("user.UserController.create"), outDto);
        return ResponseEntity.ok(outDto);
    }

    @GetMapping
    public ResponseEntity<List<UserDtoToClient>> readAll() {
        List<UserDtoToClient> dtoList = service.readAll();
        log.info("{}: {}", messageSource.get("user.UserController.readAll"),
                dtoList.stream().map(UserDtoToClient::getId).collect(Collectors.toList()));
        return ResponseEntity.ok(dtoList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDtoToClient> readById(@PathVariable Long id) {
        UserDtoToClient dto = service.readById(id);
        log.info("{}: {}", messageSource.get("user.UserController.readById"), dto);
        return ResponseEntity.ok(dto);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserDtoToClient> update(@PathVariable Long id, @RequestBody UserDtoFromClient inDto) {
        UserDtoToClient outDto = service.update(id, inDto);
        log.info("{}: {}", messageSource.get("user.UserController.update"), outDto);
        return ResponseEntity.ok(outDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        service.delete(id);
        log.info("{}: {}", messageSource.get("user.UserController.delete"), id);
        return ResponseEntity.ok("");
    }
}