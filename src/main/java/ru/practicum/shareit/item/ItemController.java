package ru.practicum.shareit.item;

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
@RequestMapping("/items")
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ItemController {
    DefaultLocaleMessageSource messageSource;
    ItemService itemService;

    @Autowired
    public ItemController(DefaultLocaleMessageSource messageSource, ItemService itemService) {
        this.messageSource = messageSource;
        this.itemService = itemService;
    }

    @PostMapping
    public ResponseEntity<ItemDto> create(@RequestHeader("X-Sharer-User-Id") Integer userId,
                                          @Valid @RequestBody ItemDto itemDto) {
        itemDto.setOwner(userId);
        itemDto = itemService.create(itemDto);
        log.info("{}: {}", messageSource.get("item.ItemController.create"), itemDto);
        return ResponseEntity.ok(itemDto);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ItemDto> create(@RequestHeader("X-Sharer-User-Id") Integer userId,
                                          @PathVariable Integer id,
                                          @Valid @RequestBody ItemDto itemDto) {
        itemDto.setId(id);
        itemDto.setOwner(userId);
        itemDto = itemService.update(itemDto);
        log.info("{}: {}", messageSource.get("item.ItemController.update"), itemDto);
        return ResponseEntity.ok(itemDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemDto> readById(@PathVariable Integer id) {
        ItemDto itemDto = itemService.readById(id);
        log.info("{}: {}", messageSource.get("item.ItemController.readById"), itemDto);
        return ResponseEntity.ok(itemDto);
    }

    @GetMapping
    public ResponseEntity<Set<ItemDto>> readByOwner(@RequestHeader("X-Sharer-User-Id") Integer userId) {
        Set<ItemDto> items = itemService.readByOwner(userId);
        log.info("{} ({}): {}", messageSource.get("user.UserController.readByOwner"), userId,
                items.stream().map(ItemDto::getId).collect(Collectors.toSet()));
        return ResponseEntity.ok(items);
    }

    @GetMapping("/search")
    public ResponseEntity<Set<ItemDto>> readByQuery(@RequestParam(value = "text", required = false) String text) {
        Set<ItemDto> items = itemService.readByQuery(text);
        log.info("{} ({}): {}", messageSource.get("user.UserController.readByQuery"), text,
                items.stream().map(ItemDto::getId).collect(Collectors.toSet()));
        return ResponseEntity.ok(items);
    }
}
