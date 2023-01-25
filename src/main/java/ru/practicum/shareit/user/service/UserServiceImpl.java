package ru.practicum.shareit.user.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.AlreadyExistException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.support.ConstraintChecker;
import ru.practicum.shareit.support.DefaultLocaleMessageSource;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserRepository;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImpl implements UserService {
    UserRepository userRepository;

    DefaultLocaleMessageSource messageSource;

    @Override
    public UserDto create(UserDto userDto) {
        if (userDto == null) {
            throw new ValidationException("user", messageSource.get("user.UserService.notNullUser"));
        }
        if (userDto.getName() == null) {
            throw new ValidationException("name", messageSource.get("user.UserService.notNullName"));
        }
        if (userDto.getEmail() == null) {
            throw new ValidationException("name", messageSource.get("user.UserService.notNullEmail"));
        }

        try {
            return UserMapper.toUserDto(userRepository.save(UserMapper.toUser(userDto)));
        } catch (DataIntegrityViolationException exception) {
            if (ConstraintChecker.check(exception, "uq_user_email")) {
                throw new AlreadyExistException("email", messageSource.get("user.UserService.notUniqueEmail") + ": " +
                        userDto.getEmail());
            } else {
                throw exception;
            }
        }
    }

    @Override
    public Set<UserDto> readAll() {
        return userRepository.findAll().stream().map(UserMapper::toUserDto)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public UserDto readById(Long id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            throw new NotFoundException("id", messageSource.get("user.UserService.notFoundById") + ": " + id);
        }
        return UserMapper.toUserDto(user.get());
    }

    @Override
    public UserDto update(UserDto userDto) {
        if (userDto == null) {
            throw new ValidationException("user", messageSource.get("user.UserService.notNullUser"));
        }
        if (userDto.getId() == null) {
            throw new ValidationException("id", messageSource.get("user.UserService.notNullId"));
        }

        Optional<User> user = userRepository.findById(userDto.getId());
        if (user.isEmpty()) {
            throw new NotFoundException("id", messageSource.get("user.UserService.notFoundById") + ": "
                    + userDto.getId());
        }

        if (userDto.getEmail() != null) {
            user.get().setEmail(userDto.getEmail());
        }
        if (userDto.getName() != null) {
            user.get().setName(userDto.getName());
        }

        try {
            return UserMapper.toUserDto(userRepository.save(user.get()));
        } catch (DataIntegrityViolationException exception) {
            if (ConstraintChecker.check(exception, "uq_user_email")) {
                throw new AlreadyExistException("email", messageSource.get("user.UserService.notUniqueEmail") + ": " +
                        userDto.getEmail());
            } else {
                throw exception;
            }
        }
    }

    @Override
    public void delete(Long id) {
        if (id == null) {
            throw new ValidationException("id", messageSource.get("user.UserService.notNullId"));
        }
        try {
            userRepository.deleteById(id);
        } catch (EmptyResultDataAccessException ignored) {
            throw new NotFoundException("id", messageSource.get("user.UserService.notFoundById") + ": " + id);
        }
    }
}