package ru.practicum.shareit.user;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.support.DefaultLocaleMessageSource;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImpl implements UserService {
    UserRepository userRepository;
    ItemRepository itemRepository;
    DefaultLocaleMessageSource messageSource;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, ItemRepository itemRepository,
                           DefaultLocaleMessageSource messageSource) {
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
        this.messageSource = messageSource;
    }

    @Override
    public UserDto create(UserDto userDto) {
        if (userDto.getName() == null) {
            throw new ValidationException("name", messageSource.get("user.UserService.notNullName"));
        }
        if (userDto.getEmail() == null) {
            throw new ValidationException("name", messageSource.get("user.UserService.notNullEmail"));
        }
        return UserMapper.toUserDto(userRepository.create(UserMapper.toUser(userDto)));
    }

    @Override
    public Set<UserDto> readAll() {
        return userRepository.readAll().stream().map(UserMapper::toUserDto)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public UserDto readById(Integer id) {
        User user = userRepository.readById(id);
        if (user == null) {
            throw new NotFoundException("id", messageSource.get("user.UserService.notFoundById") + ": " + id);
        }
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto update(UserDto userDto) {
        User user = userRepository.update(UserMapper.toUser(userDto));
        if (user == null) {
            throw new NotFoundException("id", messageSource.get("user.UserService.notFoundById") + ": "
                    + userDto.getId());
        }
        return UserMapper.toUserDto(user);
    }

    @Override
    public void delete(Integer id) {
        boolean deleted = userRepository.delete(id);
        if (!deleted) {
            throw new NotFoundException("id", messageSource.get("user.UserService.notFoundById") + ": " + id);
        }
        itemRepository.deleteByOwner(id);
    }
}