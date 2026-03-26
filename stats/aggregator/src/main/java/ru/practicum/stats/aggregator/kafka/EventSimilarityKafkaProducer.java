package ru.practicum.stats.aggregator.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventSimilarityKafkaProducer {

    private final KafkaTemplate<String, EventSimilarityAvro> kafkaTemplate;
    @Value("${spring.kafka.producer.topic.events-similarity}")
    private String topicName;

    public void sendSimilarityScores(List<EventSimilarityAvro> messages) {
        log.debug("Отправка {} сообщений в топик '{}'", messages.size(), topicName);
        List<CompletableFuture<SendResult<String, EventSimilarityAvro>>> futures = messages.stream()
                .map(message -> kafkaTemplate.send(topicName, message))
                .toList();
        for (CompletableFuture<SendResult<String, EventSimilarityAvro>> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                log.error("Ошибка при отправке сообщения в топик '{}'", topicName, e);
                throw new RuntimeException(e);
            }
        }
    }
}
