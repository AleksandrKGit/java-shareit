package ru.practicum.shareit.request;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.support.DefaultLocaleMessageSource;
import javax.validation.Valid;
import javax.validation.constraints.Min;

@Slf4j
@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Validated
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ItemRequestController {
    DefaultLocaleMessageSource messageSource;

    ItemRequestClient client;

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader("X-Sharer-User-Id") Long userId,
                                         @Valid @RequestBody ItemRequestDtoFromClient inDto) {
        log.info("{}: {}, {}", messageSource.get("itemRequest.ItemRequestController.create"), userId, inDto);
        return client.create(userId, inDto);
    }

    @GetMapping
    public ResponseEntity<Object> readByUser(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("{}: {}", messageSource.get("itemRequest.ItemRequestController.readByUser"), userId);
        return client.readByUser(userId);
    }

    @GetMapping(path = "/all")
    public ResponseEntity<Object> readAll(@RequestHeader("X-Sharer-User-Id") Long userId,
                                          @RequestParam(value = "from", required = false, defaultValue = "0")
                                          @Min(value = 0, message = "{controller.minFrom}") Integer from,
                                          @RequestParam(value = "size", required = false)
                                          @Min(value = 1, message = "{controller.minSize}") Integer size) {
        log.info("{}: {}, {}, {}", messageSource.get("itemRequest.ItemRequestController.readAll"), userId, from, size);
        return client.readAll(userId, from, size);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> readById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                           @PathVariable Long id) {
        log.info("{}: {}, {}", messageSource.get("itemRequest.ItemRequestController.readById"), userId, id);
        return client.readById(userId, id);
    }
}