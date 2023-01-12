package ru.practicum.shareit.user;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.AlreadyExistException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.support.DefaultLocaleMessageSource;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserRepositoryInMemory implements UserRepository {
    Integer id;

    Integer getId() {
        return id++;
    }

    final Map<Integer, User> users;

    final DefaultLocaleMessageSource messageSource;

    @Autowired
    public UserRepositoryInMemory(DefaultLocaleMessageSource messageSource) {
        this.messageSource = messageSource;
        id = 1;
        users = new HashMap<>();
    }

    void checkUniqueEmail(User user) {
        if (users.values().stream().anyMatch(currentUser -> !currentUser.getId().equals(user.getId())
                && currentUser.getEmail().equals(user.getEmail()))) {
            throw new AlreadyExistException("email", messageSource.get("user.UserRepository.notUniqueEmail") + ": " +
                    user.getEmail());
        }
    }

    void checkId(Integer id) {
        if (!users.containsKey(id)) {
            throw new NotFoundException("id", messageSource.get("user.UserRepository.notFoundById") + ": " + id);
        }
    }

    @Override
    public User create(User user) {
        if (user.getName() == null) {
            throw new ValidationException("name", messageSource.get("user.UserRepository.notNullName"));
        }
        if (user.getEmail() == null) {
            throw new ValidationException("name", messageSource.get("user.UserRepository.notNullEmail"));
        }
        checkUniqueEmail(user);
        user.setId(getId());
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public Set<User> readAll() {
        return users.values().stream()
                .sorted(Comparator.comparingInt(User::getId))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public User readById(Integer id) {
        checkId(id);
        return users.get(id);
    }

    @Override
    public User update(User user) {
        checkId(user.getId());
        User updatedUser = users.get(user.getId());
        if (user.getEmail() != null) {
            checkUniqueEmail(user);
            updatedUser.setEmail(user.getEmail());
        }
        if (user.getName() != null) {
            updatedUser.setName(user.getName());
        }
        return updatedUser;
    }

    @Override
    public void delete(Integer id) {
        checkId(id);
        users.remove(id);
    }
}
