package ru.practicum.shareit.user.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.AlreadyExistException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.support.ConstraintChecker;
import ru.practicum.shareit.support.DefaultLocaleMessageSource;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDtoFromClient;
import ru.practicum.shareit.user.dto.UserDtoToClient;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.UserRepository;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImpl implements UserService {
    UserRepository repository;

    DefaultLocaleMessageSource messageSource;

    UserMapper mapper;

    @Override
    public UserDtoToClient create(UserDtoFromClient dto) {
        try {
            return mapper.toDto(repository.saveAndFlush(mapper.toEntity(dto)));
        } catch (RuntimeException exception) {
            if (ConstraintChecker.check(exception, "uq_user_email")) {
                throw new AlreadyExistException("email", messageSource.get("user.UserService.notUniqueEmail") + ": " +
                        dto.getEmail());
            } else {
                throw exception;
            }
        }
    }

    @Override
    public List<UserDtoToClient> readAll() {
        return repository.findAll().stream().map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDtoToClient readById(Long id) {
        User entity = repository.findById(id).orElse(null);

        if (entity == null) {
            throw new NotFoundException("id", messageSource.get("user.UserService.notFoundById") + ": " + id);
        }

        return mapper.toDto(entity);
    }

    @Override
    public UserDtoToClient update(Long id, UserDtoFromClient dto) {
        User entity = repository.findById(id).orElse(null);

        if (entity == null) {
            throw new NotFoundException("id", messageSource.get("user.UserService.notFoundById") + ": " + id);
        }

        mapper.updateEntityFromDto(dto, entity);

        try {
            return mapper.toDto(repository.saveAndFlush(entity));
        } catch (Exception exception) {
            if (ConstraintChecker.check(exception, "uq_user_email")) {
                throw new AlreadyExistException("email", messageSource.get("user.UserService.notUniqueEmail") + ": " +
                        dto.getEmail());
            } else {
                throw exception;
            }
        }
    }

    @Override
    public void delete(Long id) {
        try {
            repository.deleteById(id);
        } catch (EmptyResultDataAccessException ignored) {
            throw new NotFoundException("id", messageSource.get("user.UserService.notFoundById") + ": " + id);
        }
    }
}