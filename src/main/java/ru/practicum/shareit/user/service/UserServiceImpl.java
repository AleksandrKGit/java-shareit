package ru.practicum.shareit.user.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.AlreadyExistException;
import ru.practicum.shareit.exception.NotFoundException;
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
        try {
            return UserMapper.INSTANCE.toDto(userRepository.save(UserMapper.INSTANCE.toModel(userDto)));
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
        return userRepository.findAll().stream().map(UserMapper.INSTANCE::toDto)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public UserDto readById(Long id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            throw new NotFoundException("id", messageSource.get("user.UserService.notFoundById") + ": " + id);
        }
        return UserMapper.INSTANCE.toDto(user.get());
    }

    @Override
    public UserDto update(Long id, UserDto userDto) {
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            throw new NotFoundException("id", messageSource.get("user.UserService.notFoundById") + ": " + id);
        }

        if (userDto.getEmail() != null) {
            user.get().setEmail(userDto.getEmail());
        }
        if (userDto.getName() != null) {
            user.get().setName(userDto.getName());
        }

        try {
            return UserMapper.INSTANCE.toDto(userRepository.save(user.get()));
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
        try {
            userRepository.deleteById(id);
        } catch (EmptyResultDataAccessException ignored) {
            throw new NotFoundException("id", messageSource.get("user.UserService.notFoundById") + ": " + id);
        }
    }
}