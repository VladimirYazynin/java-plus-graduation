package ru.practicum.stats.aggregator.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventSimilarityKafkaProducer {

    private final KafkaTemplate<String, EventSimilarityAvro> kafkaTemplate;
    @Value("${spring.kafka.producer.topic.events-similarity}")
    private String topicName;

    public void sendSimilarityScores(List<EventSimilarityAvro> messages) {
        log.debug("Отправка {} сообщений в топик '{}'", messages.size(), topicName);
        for (EventSimilarityAvro message : messages) {
            send(message);
        }
    }

    private void send(EventSimilarityAvro message) {
        try {
            kafkaTemplate.send(topicName, message).get();
            log.debug("Оценка сходства успешно отправлена: message={}", message);
        } catch (Exception e) {
            log.error("Ошибка при отправке оценки сходства в топик '{}': message={}",
                    topicName, message, e);
            throw new RuntimeException(e);
        }
    }
}
