package ru.practicum.stats.collector;

import com.google.protobuf.Timestamp;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.service.collector.ActionTypeProto;
import ru.practicum.service.collector.UserActionProto;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducer {

    private final KafkaTemplate<String, SpecificRecordBase> producer;

    @Value("${collector.kafka.producer.user-action-topic}")
    private String userActionTopic;

    public void send(UserActionProto request) {
        log.debug("Отправка сообщения в kafka topic: {}", userActionTopic);
        SpecificRecordBase message = UserActionAvro.newBuilder()
                .setUserId(request.getUserId())
                .setEventId(request.getEventId())
                .setActionType(caseActionTypeProto(request.getActionType()))
                .setTimestamp(protoTimestampToAvro(request.getTimestamp()))
                .build();
        producer.send(userActionTopic, message);
    }

    private ActionTypeAvro caseActionTypeProto(ActionTypeProto actionType) {
        return switch (actionType){
            case ACTION_VIEW -> ActionTypeAvro.VIEW;
            case ACTION_REGISTER -> ActionTypeAvro.REGISTER;
            case ACTION_LIKE -> ActionTypeAvro.LIKE;
            case UNRECOGNIZED -> null;
        };
    }

    @PreDestroy
    public void preDestroy(){
        producer.flush();
        producer.setCloseTimeout(Duration.ofSeconds(5));
        producer.destroy();
    }

    private Instant protoTimestampToAvro(Timestamp timestamp) {
        return Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
    }
}
