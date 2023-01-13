package ru.practicum.shareit.user;

import org.springframework.stereotype.Repository;
import java.util.Set;

@Repository
public interface UserRepository {
    User create(User user);

    User readById(Integer id);

    Set<User> readAll();

    User update(User user);

    boolean delete(Integer id);
}
