package ru.practicum.shareit.booking;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface BookingRepository  extends JpaRepository<Booking, Long> {
    @Query("SELECT COUNT(b.id) FROM Booking b WHERE b.item.id = ?1 AND b.status = ?2 AND (b.start < ?3 " +
            "AND b.end > ?3 OR b.start >= ?3 AND b.start < ?4)")
    Long getApprovedBookingsCountInPeriodForItem(Long itemId, BookingStatus status, LocalDateTime start,
                                                 LocalDateTime end);

    @Query("SELECT b FROM Booking b WHERE b.booker.id = ?1 AND b.start < CURRENT_TIMESTAMP " +
            "AND b.end > CURRENT_TIMESTAMP")
    Page<Booking> findCurrentForBooker(Long bookerId, Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.booker.id = ?1 AND b.end < CURRENT_TIMESTAMP")
    Page<Booking> findPastForBooker(Long bookerId, Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.booker.id = ?1 AND b.start > CURRENT_TIMESTAMP")
    Page<Booking> findFutureForBooker(Long bookerId, Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.booker.id = ?1 AND b.status = ?2")
    Page<Booking> findByStatusForBooker(Long bookerId, BookingStatus status, Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.booker.id = ?1")
    Page<Booking> findAllForBooker(Long bookerId, Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = ?1 AND b.start < CURRENT_TIMESTAMP " +
            "AND b.end > CURRENT_TIMESTAMP")
    Page<Booking> findCurrentForOwner(Long ownerId, Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = ?1 AND b.end < CURRENT_TIMESTAMP")
    Page<Booking> findPastForOwner(Long ownerId, Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = ?1 AND b.start > CURRENT_TIMESTAMP")
    Page<Booking> findFutureForOwner(Long ownerId, Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = ?1 AND b.status = ?2")
    Page<Booking> findByStatusForOwner(Long ownerId, BookingStatus status, Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = ?1")
    Page<Booking> findAllForOwner(Long ownerId, Pageable pageable);

    // Пример использования запросных методов
    // Получение последнего бронирования вещи

    Optional<Booking> findFirst1ByItem_IdAndEndLessThanOrderByEndDesc(Long itemId, LocalDateTime end);

    // Получение ближайшего следующего бронирования вещи
    Optional<Booking> findFirst1ByItem_IdAndStartGreaterThanOrderByStartAsc(Long itemId, LocalDateTime start);

    @Query("SELECT COUNT(b.id) FROM Booking b WHERE b.item.id = ?1 AND b.booker.id = ?2 " +
            "AND b.start < CURRENT_TIMESTAMP AND b.status = ?3")
    Long getItemBookingsCountForBooker(Long itemId, Long bookerId, BookingStatus status);
}
