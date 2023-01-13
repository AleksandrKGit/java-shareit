package ru.practicum.shareit.user;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.AlreadyExistException;
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

    @Override
    public User create(User user) {
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
        return users.get(id);
    }

    @Override
    public User update(User user) {
        User updatedUser = users.get(user.getId());
        if (updatedUser == null) {
            return null;
        }
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
    public boolean delete(Integer id) {
        return users.remove(id) != null;
    }
}