package ru.practicum.stats.analyzer.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.stats.analyzer.entity.EventSimilarityEntity;
import ru.practicum.stats.analyzer.entity.NeighbourResult;
import ru.practicum.stats.analyzer.model.Recommendation;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public interface EventSimilarityRepository extends JpaRepository<EventSimilarityEntity, Long> {

    @Query("""
        SELECT new ru.practicum.stats.analyzer.model.Recommendation(
            CASE WHEN es.eventA = :eventId THEN es.eventB ELSE es.eventA END,
            es.score
        )
        FROM EventSimilarityEntity es
        WHERE
            (es.eventA = :eventId OR es.eventB = :eventId)
        AND
            (CASE WHEN es.eventA = :eventId THEN es.eventB ELSE es.eventA END) NOT IN :excludeIds
        """)
    List<Recommendation> findTopSimilarExcluding(@Param("eventId") Long eventId,
                                                 @Param("excludeIds") Set<Long> excludeIds,
                                                 Pageable pageable);


    @Query("""
        SELECT new ru.practicum.stats.analyzer.model.Recommendation(
            CASE WHEN es.eventA IN :sourceIds THEN es.eventB ELSE es.eventA END,
            MAX(es.score)
        )
        FROM EventSimilarityEntity es
        WHERE
            (es.eventA IN :sourceIds OR es.eventB IN :sourceIds)
        AND
            (CASE WHEN es.eventA IN :sourceIds THEN es.eventB ELSE es.eventA END) NOT IN :excludeIds
        GROUP BY
            (CASE WHEN es.eventA IN :sourceIds THEN es.eventB ELSE es.eventA END)
        ORDER BY
            MAX(es.score) DESC
        """)
    List<Recommendation> findTopSimilarToSetExcluding(@Param("sourceIds") List<Long> sourceIds,
                                                      @Param("excludeIds") Set<Long> excludeIds,
                                                      Pageable pageable);

    default Map<Long, List<Recommendation>> findNeighbourEventsFrom(Set<Long> primaryEventIds,
                                                                    Set<Long> candidates,
                                                                    int maxNeighbours) {
        if (primaryEventIds.isEmpty() || candidates.isEmpty()) {
            return Map.of();
        }
        List<NeighbourResult> flatResult = findNeighboursNative(primaryEventIds, candidates, maxNeighbours);

        return flatResult.stream()
                .collect(Collectors.groupingBy(
                        NeighbourResult::getPrimaryId,
                        Collectors.mapping(
                                neighbour -> new Recommendation(neighbour.getNeighbourId(), neighbour.getScore()),
                                Collectors.toList()
                        )
                ));
    }

    @Query(value =
            """
            WITH pairs AS (
                SELECT
                    es.event_a as primary_id,
                    es.event_b as neighbour_id,
                    es.score
                FROM similarities es
                WHERE es.event_a IN (:primaryIds) AND es.event_b IN (:candidateIds)
                UNION ALL
                SELECT
                    es.event_b as primary_id,
                    es.event_a as neighbour_id,
                    es.score
                FROM similarities es
                WHERE es.event_b IN (:primaryIds) AND es.event_a IN (:candidateIds)
            ),
            ranked_pairs AS (
                SELECT
                    *,
                    ROW_NUMBER() OVER (PARTITION BY primary_id ORDER BY score DESC) as rn
                FROM pairs
            )
            SELECT
                primary_id as primaryId,
                neighbour_id as neighbourId,
                score
            FROM ranked_pairs
            WHERE rn <= :maxNeighbours
            """,
            nativeQuery = true)
    List<NeighbourResult> findNeighboursNative(@Param("primaryIds") Set<Long> primaryIds,
                                               @Param("candidateIds") Set<Long> candidates,
                                               @Param("maxNeighbours") int maxNeighbours);

    @Query("SELECT COUNT(s) > 0 FROM EventSimilarityEntity s " +
            "WHERE (s.eventA = :eventA AND s.eventB = :eventB) OR (s.eventA = :eventB AND s.eventB = :eventA)")
    boolean existsByEventAAndEventB(Long eventA, Long eventB);
}
