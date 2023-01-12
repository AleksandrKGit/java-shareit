package ru.practicum.shareit.user;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.ItemRepository;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImpl implements UserService {
    UserRepository userRepository;
    ItemRepository itemRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, ItemRepository itemRepository) {
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
    }

    @Override
    public UserDto create(UserDto userDto) {
        return UserMapper.toUserDto(userRepository.create(UserMapper.toUser(userDto)));
    }

    @Override
    public Set<UserDto> readAll() {
        return userRepository.readAll().stream().map(UserMapper::toUserDto)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public UserDto readById(Integer id) {
        return UserMapper.toUserDto(userRepository.readById(id));
    }

    @Override
    public UserDto update(UserDto userDto) {
        return UserMapper.toUserDto(userRepository.update(UserMapper.toUser(userDto)));
    }

    @Override
    public void delete(Integer id) {
        itemRepository.deleteByOwner(id);
        userRepository.delete(id);
    }
}