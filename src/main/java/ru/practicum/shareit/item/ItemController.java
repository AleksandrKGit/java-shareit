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
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/items")
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class ItemController {
    DefaultLocaleMessageSource messageSource;

    ItemService itemService;

    @PostMapping
    public ResponseEntity<ItemDtoToClient> create(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                  @Validated(OnCreate.class) @RequestBody ItemDtoFromClient
                                                          itemDtoFromClient) {
        ItemDtoToClient itemDtoToClient = itemService.create(userId, itemDtoFromClient);
        log.info("{}: {}", messageSource.get("item.ItemController.create"), itemDtoToClient);
        return ResponseEntity.ok(itemDtoToClient);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ItemDtoToClient> update(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                  @PathVariable Long id,
                                                  @Validated(OnUpdate.class) @RequestBody ItemDtoFromClient
                                                              itemDtoFromClient) {
        ItemDtoToClient itemDtoToClient = itemService.update(userId, id, itemDtoFromClient);
        log.info("{}: {}", messageSource.get("item.ItemController.update"), itemDtoToClient);
        return ResponseEntity.ok(itemDtoToClient);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemDtoToClient> readById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                    @PathVariable Long id) {
        ItemDtoToClient itemDtoToClient = itemService.readById(userId, id);
        log.info("{}: {}", messageSource.get("item.ItemController.readById"), itemDtoToClient);
        return ResponseEntity.ok(itemDtoToClient);
    }

    @GetMapping
    public ResponseEntity<Set<ItemDtoToClient>> readByOwner(@RequestHeader("X-Sharer-User-Id") Long userId) {
        Set<ItemDtoToClient> items = itemService.readByOwner(userId);
        log.info("{} ({}): {}", messageSource.get("item.ItemController.readByOwner"), userId,
                items.stream().map(ItemDtoToClient::getId).collect(Collectors.toSet()));
        return ResponseEntity.ok(items);
    }

    @GetMapping("/search")
    public ResponseEntity<Set<ItemDtoToClient>> readByQuery(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                            @RequestParam(value = "text", required = false)
                                                            String text) {
        Set<ItemDtoToClient> items = itemService.readByQuery(userId, text);
        log.info("{} ({}): {}", messageSource.get("item.ItemController.readByQuery"), text,
                items.stream().map(ItemDtoToClient::getId).collect(Collectors.toSet()));
        return ResponseEntity.ok(items);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<CommentDtoToClient> createComment(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                            @PathVariable Long itemId, @Valid @RequestBody
                                                                CommentDtoFromClient commentDtoFromClient) {
        CommentDtoToClient commentDtoToClient = itemService.createComment(userId, itemId, commentDtoFromClient);
        log.info("{}: {}", messageSource.get("item.ItemController.createComment"), commentDtoToClient);
        return ResponseEntity.ok(commentDtoToClient);
    }
}
