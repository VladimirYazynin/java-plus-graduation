package ru.practicum.stats.aggregator.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.stats.aggregator.kafka.EventSimilarityKafkaProducer;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AggregatorService {

    private final Map<Long, Map<Long, Double>> scalarResultMatrix;
    private final Map<Long, Map<Long, Double>> eventUserWeights;
    private final Map<ActionTypeAvro, Double> actionWeights;
    private final EventSimilarityKafkaProducer producer;

    public AggregatorService(EventSimilarityKafkaProducer producer, WeightProperties weightProperties) {
        this.eventUserWeights = new HashMap<>();
        this.scalarResultMatrix = new HashMap<>();
        this.producer = producer;
        this.actionWeights = weightProperties.getWeights();
    }

    public void calculateSimilarity(UserActionAvro userAction) {
        double newWeight = getWeight(userAction.getActionType());
        List<EventSimilarityAvro> similarities = updateEventWeight(
                userAction.getEventId(),
                userAction.getUserId(),
                newWeight
        );
        producer.sendSimilarityScores(similarities.stream()
                .sorted(Comparator.comparingLong(EventSimilarityAvro::getEventA)
                        .thenComparingLong(EventSimilarityAvro::getEventB))
                .collect(Collectors.toList()));
    }

    private double getWeight(ActionTypeAvro actionType) {
        return this.actionWeights.getOrDefault(actionType, 0.0);
    }

    private List<EventSimilarityAvro> updateEventWeight(Long eventId, Long userId, Double newWeight) {
        Map<Long, Double> userWeights = eventUserWeights.computeIfAbsent(eventId, k -> new HashMap<>());
        Double currentWeight = userWeights.get(userId);
        if (currentWeight == null || currentWeight < newWeight) {
            List<EventSimilarityAvro> updatedSimilarities = recalculateSimilarities(
                    eventId,
                    userId,
                    newWeight,
                    currentWeight
            );
            userWeights.put(userId, newWeight);
            return updatedSimilarities;
        }
        return Collections.emptyList();
    }

    private List<EventSimilarityAvro> recalculateSimilarities(Long eventId, Long userId,
                                                              Double newWeight, Double oldWeight) {
        Map<Long, Double> selfDotProducts = scalarResultMatrix.computeIfAbsent(eventId, k -> new HashMap<>());
        double currentSelfProduct = selfDotProducts.getOrDefault(eventId, 0.0);
        double oldW = (oldWeight == null) ? 0.0 : oldWeight;
        double selfDelta = newWeight * newWeight - oldW * oldW;
        selfDotProducts.put(eventId, currentSelfProduct + selfDelta);
        return updateCrossDotProducts(eventId, userId, newWeight, oldWeight);
    }

    private List<EventSimilarityAvro> updateCrossDotProducts(Long updatedEventId, Long userId,
                                                             Double newWeight, Double oldWeight) {
        List<EventSimilarityAvro> updatedSimilarities = new ArrayList<>();
        for (Long otherEventId : eventUserWeights.keySet()) {
            if (updatedEventId.equals(otherEventId)) {
                continue;
            }
            long eventA, eventB;
            if (updatedEventId < otherEventId) {
                eventA = updatedEventId;
                eventB = otherEventId;
            } else {
                eventA = otherEventId;
                eventB = updatedEventId;
            }
            Map<Long, Double> otherUserWeights = eventUserWeights.get(otherEventId);
            if (otherUserWeights != null) {
                Double otherWeight = otherUserWeights.get(userId);
                if (otherWeight != null) {
                    EventSimilarityAvro similarity = updateDotProductForPair(
                            eventA, eventB, newWeight, oldWeight, otherWeight
                    );
                    if (similarity != null) {
                        updatedSimilarities.add(similarity);
                    }
                }
            }
        }
        return updatedSimilarities;
    }

    private EventSimilarityAvro updateDotProductForPair(long eventA, long eventB, Double newWeight, Double oldWeight,
                                                        Double otherWeight) {
        Map<Long, Double> dotProducts = scalarResultMatrix.computeIfAbsent(eventA, k -> new HashMap<>());
        double currentDotProduct = dotProducts.getOrDefault(eventB, 0.0);
        double oldMinWeight = (oldWeight == null) ? 0.0 : Math.min(oldWeight, otherWeight);
        double newMinWeight = Math.min(newWeight, otherWeight);
        double dotProductDelta = newMinWeight - oldMinWeight;
        double updatedDotProduct = currentDotProduct + dotProductDelta;
        dotProducts.put(eventB, updatedDotProduct);
        return calculateSimilarity(eventA, eventB, updatedDotProduct);
    }

    private EventSimilarityAvro calculateSimilarity(long eventA, long eventB, double dotProduct) {
        Double magnitudeA = calculateMagnitude(eventA);
        Double magnitudeB = calculateMagnitude(eventB);
        if (magnitudeA == null || magnitudeB == null || magnitudeA == 0 || magnitudeB == 0) {
            return null;
        }
        double similarity = dotProduct / (magnitudeA * magnitudeB);
        return new EventSimilarityAvro(eventA, eventB, similarity, Instant.now());
    }

    private Double calculateMagnitude(Long eventId) {
        Map<Long, Double> selfDotProducts = scalarResultMatrix.get(eventId);
        if (selfDotProducts == null) return null;
        Double selfProduct = selfDotProducts.get(eventId);
        return (selfProduct != null) ? Math.sqrt(selfProduct) : null;
    }
}