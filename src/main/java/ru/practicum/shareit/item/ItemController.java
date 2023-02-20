package ru.practicum.shareit.item;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDtoFromClient;
import ru.practicum.shareit.item.dto.CommentDtoToClient;
import ru.practicum.shareit.item.dto.ItemDtoFromClient;
import ru.practicum.shareit.item.dto.ItemDtoToClient;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.support.DefaultLocaleMessageSource;
import ru.practicum.shareit.validation.groups.OnCreate;
import ru.practicum.shareit.validation.groups.OnUpdate;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Validated
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ItemController {
    DefaultLocaleMessageSource messageSource;

    ItemService service;

    @PostMapping
    public ResponseEntity<ItemDtoToClient> create(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                  @Validated(OnCreate.class) @RequestBody ItemDtoFromClient inDto) {
        ItemDtoToClient outDto = service.create(userId, inDto);
        log.info("{}: {}", messageSource.get("item.ItemController.create"), outDto);
        return ResponseEntity.ok(outDto);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<CommentDtoToClient> createComment(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                            @PathVariable Long itemId, @Valid @RequestBody
                                                            CommentDtoFromClient inDto) {
        CommentDtoToClient outDto = service.createComment(userId, itemId, inDto);
        log.info("{}: {}", messageSource.get("item.ItemController.createComment"), outDto);
        return ResponseEntity.ok(outDto);
    }

    @GetMapping
    public ResponseEntity<List<ItemDtoToClient>> readByOwner(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                             @RequestParam(value = "from", required = false,
                                                                     defaultValue = "0")
                                                             @Min(value = 0, message = "{controller.minFrom}")
                                                             Integer from,
                                                             @RequestParam(value = "size", required = false)
                                                             @Min(value = 1, message = "{controller.minSize}")
                                                             Integer size) {
        List<ItemDtoToClient> dtoList = service.readByOwner(userId, from, size);
        log.info("{} ({}): {}", messageSource.get("item.ItemController.readByOwner"), userId,
                dtoList.stream().map(ItemDtoToClient::getId).collect(Collectors.toList()));
        return ResponseEntity.ok(dtoList);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ItemDtoToClient>> readByQuery(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                             @RequestParam(value = "from", required = false,
                                                                     defaultValue = "0")
                                                             @Min(value = 0, message = "{controller.minFrom}")
                                                             Integer from,
                                                             @RequestParam(value = "size", required = false)
                                                             @Min(value = 1, message = "{controller.minSize}")
                                                             Integer size,
                                                             @RequestParam(value = "text", required = false)
                                                             String text) {
        List<ItemDtoToClient> dtoList = service.readByQuery(userId, text, from, size);
        log.info("{} ({}): {}", messageSource.get("item.ItemController.readByQuery"), text,
                dtoList.stream().map(ItemDtoToClient::getId).collect(Collectors.toList()));
        return ResponseEntity.ok(dtoList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemDtoToClient> readById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                    @PathVariable Long id) {
        ItemDtoToClient dto = service.readById(userId, id);
        log.info("{}: {}", messageSource.get("item.ItemController.readById"), dto);
        return ResponseEntity.ok(dto);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ItemDtoToClient> update(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                  @PathVariable Long id,
                                                  @Validated(OnUpdate.class) @RequestBody ItemDtoFromClient inDto) {
        ItemDtoToClient outDto = service.update(userId, id, inDto);
        log.info("{}: {}", messageSource.get("item.ItemController.update"), outDto);
        return ResponseEntity.ok(outDto);
    }
}
