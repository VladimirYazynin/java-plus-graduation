package ru.practicum.stats.analyzer.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.stats.analyzer.entity.UserActionEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public interface UserActionRepository extends JpaRepository<UserActionEntity, Long> {

    Optional<UserActionEntity> findByUserIdAndEventId(long userId, long eventId);

    @Query("SELECT ua.eventId FROM UserActionEntity ua WHERE ua.userId = :userId")
    Set<Long> findEventIdsByUserId(@Param("userId") Long userId);

    @Query("SELECT ua.eventId FROM UserActionEntity ua WHERE ua.userId = :userId")
    List<Long> findRecentEventIdsByUserId(Long userId, Pageable pageable);

    default Map<Long, Double> findWeightsByUserIdAndEventIds(Long userId, Set<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Map.of();
        }
        return findActionWeights(userId, eventIds).stream()
                .collect(Collectors.toMap(obj -> (Long) obj[0], obj -> (Double) obj[1]));
    }

    @Query("SELECT ua.eventId, ua.actionWeight FROM UserActionEntity ua WHERE ua.userId = :userId AND ua.eventId IN :eventIds")
    List<Object[]> findActionWeights(@Param("userId") Long userId, @Param("eventIds") Set<Long> eventIds);

    default Map<Long, Double> getAggregatedWeightsForEvents(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Map.of();
        }
        return getSumOfWeights(eventIds).stream()
                .collect(Collectors.toMap(obj -> (Long) obj[0], obj -> (Double) obj[1]));
    }

    @Query("""
            SELECT ua.eventId, SUM(ua.actionWeight)
            FROM UserActionEntity ua
            WHERE ua.eventId IN :eventIds
            GROUP BY ua.eventId
        """)
    List<Object[]> getSumOfWeights(@Param("eventIds") List<Long> eventIds);
}
