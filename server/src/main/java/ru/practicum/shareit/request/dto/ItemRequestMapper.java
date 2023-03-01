package ru.practicum.shareit.request.dto;

import org.mapstruct.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.ItemRequest;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ItemRequestMapper {
    @Mapping(target = "items", ignore = true)
    ItemRequestDtoToClient toDto(ItemRequest entity, @Context ItemRepository repository);

    List<ItemRequestDtoToClient> toDtoList(List<ItemRequest> entities, @Context ItemRepository repository);

    @AfterMapping
    default void toDto(@MappingTarget ItemRequestDtoToClient target, ItemRequest itemRequest,
                       @Context ItemRepository repository) {
        target.setItems(itemRequest == null || itemRequest.getId() == null || repository == null ? null :
                toItemDtoList(repository.findByRequest_IdOrderByIdAsc(itemRequest.getId())));
    }

    List<ItemDtoToClient> toItemDtoList(List<Item> entities);

    @Mapping(target = "requestId", source = "request.id")
    ItemDtoToClient toItemDto(Item entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "created", ignore = true)
    @Mapping(target = "requestor", ignore = true)
    ItemRequest toEntity(ItemRequestDtoFromClient dto);
}