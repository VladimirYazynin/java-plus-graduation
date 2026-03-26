package ru.practicum.stats.analyzer.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.service.dashboard.InteractionsCountRequestProto;
import ru.practicum.service.dashboard.RecommendedEventProto;
import ru.practicum.service.dashboard.SimilarEventsRequestProto;
import ru.practicum.service.dashboard.UserPredictionsRequestProto;
import ru.practicum.stats.analyzer.model.Recommendation;
import ru.practicum.stats.analyzer.repository.EventSimilarityRepository;
import ru.practicum.stats.analyzer.repository.UserActionRepository;
import ru.practicum.stats.analyzer.service.RecommendationsService;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationsServiceImpl implements RecommendationsService {

    private final UserActionRepository userActionRepository;
    private final EventSimilarityRepository eventSimilarityRepository;

    @Override
    public List<RecommendedEventProto> getRecommendationsForUser(UserPredictionsRequestProto request) {
        Long userId = request.getUserId();
        int limit = request.getMaxResults();
        log.debug("Запрос персонализированных рекомендаций для userId: {}, limit: {}", userId, limit);
        Pageable recentInteractionsPageable = PageRequest.of(0, limit);
        List<Long> recentEventIds = userActionRepository.findRecentEventIdsByUserId(userId, recentInteractionsPageable);
        if (recentEventIds.isEmpty()) {
            log.warn("Для userId={} не найдено недавних действий.", userId);
            return List.of();
        }
        Set<Long> allUserEvents = userActionRepository.findEventIdsByUserId(userId);
        Pageable candidatesPageable = PageRequest.of(0, limit);
        List<Recommendation> candidateRecs = eventSimilarityRepository.findTopSimilarToSetExcluding(
                recentEventIds,
                allUserEvents,
                candidatesPageable
        );
        Set<Long> candidateEventIds = candidateRecs.stream().map(Recommendation::getEventId).collect(Collectors.toSet());
        if (candidateEventIds.isEmpty()) {
            log.warn("Не найдено новых кандидатов для рекомендаций для userId={}", userId);
            return List.of();
        }
        Map<Long, List<Recommendation>> neighboursMap = eventSimilarityRepository.findNeighbourEventsFrom(
                candidateEventIds,
                allUserEvents,
                limit
        );
        Set<Long> allNeighbourIds = neighboursMap.values().stream()
                .flatMap(List::stream)
                .map(Recommendation::getEventId)
                .collect(Collectors.toSet());
        Map<Long, Double> userRatings = userActionRepository.findWeightsByUserIdAndEventIds(userId, allNeighbourIds);
        List<RecommendedEventProto> finalRecommendations = candidateEventIds.stream()
                .map(candidateId -> {
                    List<Recommendation> neighbours = neighboursMap.getOrDefault(candidateId, List.of());
                    if (neighbours.isEmpty()) return null;

                    double weightedSum = 0.0;
                    double similaritySum = 0.0;
                    for (Recommendation neighbour : neighbours) {
                        Double rating = userRatings.get(neighbour.getEventId());
                        if (rating != null) {
                            weightedSum += rating * neighbour.getScore();
                            similaritySum += neighbour.getScore();
                        }
                    }

                    if (similaritySum == 0) return null;

                    return RecommendedEventProto.newBuilder()
                            .setEventId(candidateId)
                            .setScore((float) (weightedSum / similaritySum))
                            .build();
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(RecommendedEventProto::getScore).reversed())
                .limit(limit) // финальное ограничение результата
                .collect(Collectors.toList());
        log.debug("Сформировано {} рекомендаций для userId: {}", finalRecommendations.size(), userId);
        return finalRecommendations;
    }

    @Override
    public List<RecommendedEventProto> getSimilarEvents(SimilarEventsRequestProto request) {
        Long eventId = request.getEventId();
        Long userId = request.getUserId();
        int limit = request.getMaxResults();
        log.debug("Запрос похожих событий для eventId: {}, исключая для userId: {}, limit: {}", eventId, userId, limit);
        Set<Long> seenEventIds = userActionRepository.findEventIdsByUserId(userId);
        seenEventIds.add(eventId);
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "score"));
        List<Recommendation> similarEvents = eventSimilarityRepository.findTopSimilarExcluding(
                eventId,
                seenEventIds,
                pageable
        );
        log.debug("Найдено {} похожих событий для мероприятий с id: {}", similarEvents.size(), eventId);
        return similarEvents.stream()
                .map(rec -> RecommendedEventProto.newBuilder()
                        .setEventId(rec.getEventId())
                        .setScore(rec.getScore().floatValue())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<RecommendedEventProto> getInteractionsCount(InteractionsCountRequestProto request) {
        List<Long> eventIds = request.getEventIdList();
        if (eventIds.isEmpty()) {
            return List.of();
        }
        log.debug("Запрос суммы весов взаимодействий для {} событий", eventIds.size());
        Map<Long, Double> eventWeights = userActionRepository.getAggregatedWeightsForEvents(eventIds);
        return eventIds.stream()
                .map(eventId -> RecommendedEventProto.newBuilder()
                        .setEventId(eventId)
                        .setScore(eventWeights.getOrDefault(eventId, 0.0).floatValue())
                        .build())
                .sorted(Comparator.comparing(RecommendedEventProto::getScore).reversed())
                .collect(Collectors.toList());
    }
}
