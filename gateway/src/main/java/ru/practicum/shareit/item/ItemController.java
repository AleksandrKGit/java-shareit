package ru.practicum.shareit.item;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDtoFromClient;
import ru.practicum.shareit.item.dto.ItemDtoFromClient;
import ru.practicum.shareit.support.DefaultLocaleMessageSource;
import ru.practicum.shareit.validation.groups.OnCreate;
import ru.practicum.shareit.validation.groups.OnUpdate;
import javax.validation.Valid;
import javax.validation.constraints.Min;

@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Validated
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ItemController {
    DefaultLocaleMessageSource messageSource;

    ItemClient client;

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader("X-Sharer-User-Id") Long userId,
                                         @Validated(OnCreate.class) @RequestBody ItemDtoFromClient inDto) {
        log.info("{}: {}, {}", messageSource.get("item.ItemController.create"), userId, inDto);
        return client.create(userId, inDto);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> createComment(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                @PathVariable Long itemId,
                                                @Valid @RequestBody CommentDtoFromClient inDto) {
        log.info("{}: {}, {}, {}", messageSource.get("item.ItemController.createComment"), userId, itemId, inDto);
        return client.createComment(userId, itemId, inDto);
    }

    @GetMapping
    public ResponseEntity<Object> readByOwner(@RequestHeader("X-Sharer-User-Id") Long userId,
                                              @RequestParam(value = "from", required = false, defaultValue = "0")
                                              @Min(value = 0, message = "{controller.minFrom}") Integer from,
                                              @RequestParam(value = "size", required = false)
                                              @Min(value = 1, message = "{controller.minSize}") Integer size) {
        log.info("{}: {}, {}, {}", messageSource.get("item.ItemController.readByOwner"), userId, from, size);
        return client.readByOwner(userId, from, size);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> readByQuery(@RequestHeader("X-Sharer-User-Id") Long userId,
                                              @RequestParam(value = "from", required = false, defaultValue = "0")
                                              @Min(value = 0, message = "{controller.minFrom}") Integer from,
                                              @RequestParam(value = "size", required = false)
                                              @Min(value = 1, message = "{controller.minSize}") Integer size,
                                              @RequestParam(value = "text", required = false) String text) {
        log.info("{}: {}, {}, {}, {}", messageSource.get("item.ItemController.readByQuery"), userId, text, from, size);
        return client.readByQuery(userId, text, from, size);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> readById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                           @PathVariable Long id) {
        log.info("{}: {}, {}", messageSource.get("item.ItemController.readById"), userId, id);
        return client.readById(userId, id);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> update(@RequestHeader("X-Sharer-User-Id") Long userId,
                                         @PathVariable Long id,
                                         @Validated(OnUpdate.class) @RequestBody ItemDtoFromClient inDto) {
        log.info("{}: {}, {}, {}", messageSource.get("item.ItemController.update"), userId, id, inDto);
        return client.update(userId, id, inDto);
    }
}