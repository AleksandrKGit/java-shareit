package ru.practicum.shareit.item.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;
import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    Page<Item> findByOwner_Id(Long ownerId, Pageable pageable);

    List<Item> findByRequest_IdOrderByIdAsc(Long requestId);

    @Query("SELECT i FROM Item i WHERE i.available = TRUE AND (lower(i.name) like lower(concat('%', ?1,'%'))" +
            "OR lower(i.description) like lower(concat('%', ?1,'%')))")
    Page<Item> findByQuery(String query, Pageable pageable);
}