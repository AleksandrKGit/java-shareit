package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

public interface BookingRepository  extends JpaRepository<Booking, Long> {
    @Query("SELECT COUNT(b.id) FROM Booking b WHERE b.item.id = ?1 AND b.status = ?2 AND (b.start < ?3 " +
            "AND b.end > ?3 OR b.start >= ?3 AND b.start < ?4)")
    Long getApprovedBookingsCountInPeriodForItem(Long itemId, BookingStatus status, LocalDateTime start,
                                                 LocalDateTime end);

    @Query("SELECT b FROM Booking b WHERE b.booker.id = ?1 AND b.start < CURRENT_TIMESTAMP " +
            "AND b.end > CURRENT_TIMESTAMP ORDER BY b.start DESC")
    Set<Booking> findCurrentForBooker(Long bookerId);

    @Query("SELECT b FROM Booking b WHERE b.booker.id = ?1 AND b.end < CURRENT_TIMESTAMP ORDER BY b.start DESC")
    Set<Booking> findPastForBooker(Long bookerId);

    @Query("SELECT b FROM Booking b WHERE b.booker.id = ?1 AND b.start > CURRENT_TIMESTAMP ORDER BY b.start DESC")
    Set<Booking> findFutureForBooker(Long bookerId);

    @Query("SELECT b FROM Booking b WHERE b.booker.id = ?1 AND b.status = ?2 ORDER BY b.start DESC")
    Set<Booking> findByStatusForBooker(Long bookerId, BookingStatus status);

    @Query("SELECT b FROM Booking b WHERE b.booker.id = ?1 ORDER BY b.start DESC")
    Set<Booking> findAllForBooker(Long bookerId);

    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = ?1 AND b.start < CURRENT_TIMESTAMP " +
            "AND b.end > CURRENT_TIMESTAMP ORDER BY b.start DESC")
    Set<Booking> findCurrentForOwner(Long ownerId);

    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = ?1 AND b.end < CURRENT_TIMESTAMP ORDER BY b.start DESC")
    Set<Booking> findPastForOwner(Long ownerId);

    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = ?1 AND b.start > CURRENT_TIMESTAMP ORDER BY b.start DESC")
    Set<Booking> findFutureForOwner(Long ownerId);

    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = ?1 AND b.status = ?2 ORDER BY b.start DESC")
    Set<Booking> findByStatusForOwner(Long ownerId, BookingStatus status);

    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = ?1 ORDER BY b.start DESC")
    Set<Booking> findAllForOwner(Long ownerId);

    // Пример использования запросных методов
    // Получение последнего бронирования вещи
    Optional<Booking> findFirst1ByItem_IdAndEndLessThanOrderByEndDesc(Long itemId, LocalDateTime end);

    // Получение ближайшего следующего бронирования вещи
    Optional<Booking> findFirst1ByItem_IdAndStartGreaterThanOrderByStartAsc(Long itemId, LocalDateTime start);

    @Query("SELECT COUNT(b.id) FROM Booking b WHERE b.item.id = ?1 AND b.booker.id = ?2 " +
            "AND b.start < CURRENT_TIMESTAMP AND b.status = ?3")
    Long getItemBookingsCountForBooker(Long itemId, Long bookerId, BookingStatus status);
}
