package ru.practicum.stats.aggregator.kafka;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.stats.aggregator.service.AggregatorService;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserActionKafkaListener {

    private final AggregatorService similarityService;

    @KafkaListener(
            topics = "${spring.kafka.consumer.topic.user-actions}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void handleUserAction(UserActionAvro message) {
        log.debug("Получено сообщение: {}", message);
        try {
            similarityService.calculateSimilarity(message);
        } catch (Exception e) {
            log.error("Ошибка при обработке сообщения: {}", message, e);
        }
    }
}
