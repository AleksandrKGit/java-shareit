package ru.practicum.shareit.request;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDtoFromClient;
import ru.practicum.shareit.request.dto.ItemRequestDtoToClient;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.support.DefaultLocaleMessageSource;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ItemRequestController {
    DefaultLocaleMessageSource messageSource;

    ItemRequestService service;

    @PostMapping
    public ResponseEntity<ItemRequestDtoToClient> create(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                         @RequestBody ItemRequestDtoFromClient inDto) {
        ItemRequestDtoToClient outDto = service.create(userId, inDto);
        log.info("{}: {}", messageSource.get("itemRequest.ItemRequestController.create"), outDto);
        return ResponseEntity.ok(outDto);
    }

    @GetMapping
    public ResponseEntity<List<ItemRequestDtoToClient>> readByUser(@RequestHeader("X-Sharer-User-Id") Long userId) {
        List<ItemRequestDtoToClient> dtoList = service.readByUser(userId);
        log.info("{} ({}): {}", messageSource.get("itemRequest.ItemRequestController.readByUser"), userId,
                dtoList.stream().map(ItemRequestDtoToClient::getId).collect(Collectors.toList()));
        return ResponseEntity.ok(dtoList);
    }

    @GetMapping(path = "/all")
    public ResponseEntity<List<ItemRequestDtoToClient>> readAll(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                               @RequestParam(value = "from", required = false,
                                                                       defaultValue = "0")
                                                               Integer from,
                                                               @RequestParam(value = "size", required = false)
                                                               Integer size) {
        List<ItemRequestDtoToClient> dtoList = service.readAll(userId, from, size);
        log.info("{} ({}, {}, {}): {}", messageSource.get("itemRequest.ItemRequestController.readAll"), userId, from,
                size, dtoList.stream().map(ItemRequestDtoToClient::getId).collect(Collectors.toList()));
        return ResponseEntity.ok(dtoList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemRequestDtoToClient> readById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                           @PathVariable Long id) {
        ItemRequestDtoToClient dto = service.readById(userId, id);
        log.info("{}: {}", messageSource.get("itemRequest.ItemRequestController.readById"), dto);
        return ResponseEntity.ok(dto);
    }
}