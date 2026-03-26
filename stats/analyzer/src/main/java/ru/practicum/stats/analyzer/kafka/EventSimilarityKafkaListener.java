package ru.practicum.stats.analyzer.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.stats.analyzer.service.SimilarityService;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventSimilarityKafkaListener {

    private final SimilarityService similarityService;

    @KafkaListener(
            topics = "${spring.kafka.similarity-consumer.topic-events-similarity}",
            containerFactory = "similarityKafkaListenerFactory"
    )
    public void handleSimilarity(@Payload EventSimilarityAvro avro, Acknowledgment acknowledgment) {
        log.debug("Прочитан коэффициент схожести, value: {}", avro);
        try {
            similarityService.handleSimilarity(avro);
            acknowledgment.acknowledge();
            log.debug("Коэффициент схожести успешно обработан: eventA: {}, eventB: {}",
                    avro.getEventA(), avro.getEventB());
        } catch (DataIntegrityViolationException e) {
            log.warn("Нарушение целостности данных для коэффициента: {}, ошибка: {}", avro, e.getMessage());
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Критическая ошибка при обработке коэффициента схожести: {}", avro, e);
            acknowledgment.acknowledge();
        }
    }
}
