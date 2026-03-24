package ru.practicum.stats.analyzer.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.stats.analyzer.service.UserActionService;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserActionKafkaListener {

    private final UserActionService userActionService;

    @KafkaListener(
            topics = "${spring.kafka.user-consumer.topic-user-actions}",
            containerFactory = "userActionKafkaListenerFactory")
    public void handleUserAction(@Payload UserActionAvro avro, Acknowledgment acknowledgment) {
        log.debug("Прочитано действие пользователя, value: {}", avro);
        try {
            userActionService.handleUserAction(avro);
            acknowledgment.acknowledge();
            log.debug("Действие пользователя успешно обработано: userId: {}, eventId: {}",
                    avro.getUserId(), avro.getEventId());
        } catch (DataIntegrityViolationException e) {
            log.warn("Нарушение целостности данных для действия: {}, ошибка: {}", avro, e.getMessage());
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Критическая ошибка при обработке действия пользователя: {}", avro, e);
            acknowledgment.acknowledge();
        }
    }
}
