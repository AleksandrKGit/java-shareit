package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;
import java.util.Set;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    Set<Item> findByOwner_IdOrderByIdAsc(Long ownerId);

    @Query("SELECT i FROM Item i WHERE i.available = TRUE AND (lower(i.name) like lower(concat('%', ?1,'%'))" +
            "OR lower(i.description) like lower(concat('%', ?1,'%'))) ORDER BY i.id ASC")
    Set<Item> findByQuery(String query);
}