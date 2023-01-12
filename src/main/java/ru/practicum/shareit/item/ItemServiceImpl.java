package ru.practicum.shareit.item;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.support.DefaultLocaleMessageSource;
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
        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(userRepository.readById(itemDto.getOwner()));
        return ItemMapper.toItemDto(itemRepository.create(item));
    }

    @Override
    public ItemDto readById(Integer id) {
        return ItemMapper.toItemDto(itemRepository.readById(id));
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
        return ItemMapper.toItemDto(itemRepository.updateByOwner(itemDto.getOwner(), ItemMapper.toItem(itemDto)));
    }
}