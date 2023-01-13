package ru.practicum.shareit.item;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.support.DefaultLocaleMessageSource;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ItemServiceImpl implements ItemService {
    ItemRepository itemRepository;
    UserRepository userRepository;
    DefaultLocaleMessageSource messageSource;

    @Autowired
    public ItemServiceImpl(DefaultLocaleMessageSource messageSource, ItemRepository itemRepository,
                           UserRepository userRepository) {
        this.messageSource = messageSource;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
    }

    @Override
    public ItemDto create(ItemDto itemDto) {
        if (itemDto.getOwner() == null) {
            throw new ValidationException("owner", messageSource.get("item.ItemService.notNullOwner"));
        }
        if (itemDto.getName() == null) {
            throw new ValidationException("name", messageSource.get("item.ItemService.notNullName"));
        }
        if (itemDto.getDescription() == null) {
            throw new ValidationException("description", messageSource.get("item.ItemService.notNullDescription"));
        }
        if (itemDto.getAvailable() == null) {
            throw new ValidationException("description", messageSource.get("item.ItemService.notNullAvailable"));
        }
        User owner = userRepository.readById(itemDto.getOwner());
        if (owner == null) {
            throw new NotFoundException("id", messageSource.get("item.ItemService.notFoundOwnerById") + ": "
                    + itemDto.getOwner());
        }
        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(owner);
        return ItemMapper.toItemDto(itemRepository.create(item));
    }

    @Override
    public ItemDto readById(Integer id) {
        Item item = itemRepository.readById(id);
        if (item == null) {
            throw new NotFoundException("id", messageSource.get("item.ItemService.notFoundById") + ": " + id);
        }
        return ItemMapper.toItemDto(item);
    }

    @Override
    public Set<ItemDto> readByOwner(Integer ownerId) {
        return itemRepository.readByOwnerId(ownerId).stream().map(ItemMapper::toItemDto)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public Set<ItemDto> readByQuery(String query) {
        return itemRepository.readByQuery(query).stream().map(ItemMapper::toItemDto)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public ItemDto update(ItemDto itemDto) {
        if (itemDto.getOwner() == null) {
            throw new ValidationException("owner", messageSource.get("item.ItemService.notNullOwner"));
        }
        if (itemDto.getId() == null) {
            throw new ValidationException("id", messageSource.get("item.ItemService.notNullId"));
        }
        Item item = itemRepository.updateByOwner(itemDto.getOwner(), ItemMapper.toItem(itemDto));
        if (item == null) {
            throw new AccessDeniedException("user" + itemDto.getOwner(), "item" + itemDto.getId().toString());
        }
        return ItemMapper.toItemDto(item);
    }
}