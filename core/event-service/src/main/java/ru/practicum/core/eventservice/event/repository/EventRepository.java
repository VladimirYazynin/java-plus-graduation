package ru.practicum.core.eventservice.event.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.core.eventservice.category.model.Category;
import ru.practicum.core.eventservice.event.model.Event;
import ru.practicum.core.interactionapi.enums.State;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    Page<Event> findAllByInitiator(Long initiatorId, Pageable page);

    boolean existsByCategory(Category category);

    @Query("""
                SELECT e
                FROM Event AS e
                WHERE e.state = 'PUBLISHED'
                AND (?1 IS NULL OR e.annotation ILIKE %?1% OR e.description ILIKE %?1%)
                AND (?2 IS NULL OR e.category.id IN ?2)
                AND (?3 IS NULL OR e.paid = ?3)
                AND (CAST(?4 AS timestamp) IS NULL AND e.eventDate >= CURRENT_TIMESTAMP OR e.eventDate >= ?4)
                AND (CAST(?5 AS timestamp) IS NULL OR e.eventDate < ?5)
                AND (?6 = false OR e.participantLimit = 0 OR e.participantLimit > e.confirmedRequests)
            """)
    List<Event> findAllByFilters(String text, List<Long> categoryIds, Boolean paid, LocalDateTime rangeStart,
                                 LocalDateTime rangeEnd, Boolean onlyAvailable, Pageable pageable);

    @Query("""
                SELECT e
                FROM Event AS e
                WHERE (?1 IS NULL OR e.initiator IN ?1)
                AND (?2 IS NULL OR e.state IN ?2)
                AND (?3 IS NULL OR e.category.id IN ?3)
                AND (CAST(?4 AS timestamp) IS NULL OR e.eventDate >= ?4)
                AND (CAST(?5 AS timestamp) IS NULL OR e.eventDate < ?5)
            """)
    Page<Event> findAllByFiltersAdmin(List<Long> userIds, List<State> states, List<Long> categoryIds,
                                      LocalDateTime rangeStart, LocalDateTime rangeEnd, Pageable pageable);
}
