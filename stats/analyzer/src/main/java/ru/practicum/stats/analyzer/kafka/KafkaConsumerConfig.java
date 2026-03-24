package ru.practicum.stats.analyzer.kafka;

import lombok.Data;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.HashMap;
import java.util.Map;

@Data
@EnableKafka
@Configuration
@ConfigurationProperties("spring.kafka")
public class KafkaConsumerConfig {

    private UserConsumer userConsumer = new UserConsumer();
    private SimilarityConsumer similarityConsumer = new SimilarityConsumer();

    @Bean(name = "userActionKafkaListenerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, UserActionAvro> userKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, UserActionAvro> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(userConsumerFactory());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        return factory;
    }

    @Bean
    public ConsumerFactory<String, UserActionAvro> userConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, userConsumer.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, userConsumer.getGroupId());
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, userConsumer.getClientId());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ru.practicum.stats.serialization.avroschemas.deserializer.UserActionDeserializer.class);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, userConsumer.isAutoCommit());
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConsumerFactory<String, EventSimilarityAvro> similarityConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, similarityConsumer.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, similarityConsumer.getGroupId());
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, similarityConsumer.getClientId());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ru.practicum.stats.serialization.avroschemas.deserializer.EventSimilarityDeserializer.class);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, similarityConsumer.isAutoCommit());
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean(name = "similarityKafkaListenerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, EventSimilarityAvro> similarityKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, EventSimilarityAvro> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(similarityConsumerFactory());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        return factory;
    }

    @Data
    public static class UserConsumer {
        private String bootstrapServers;
        private String groupId;
        private String clientId;
        private boolean autoCommit;
        private String keyDeserializer;
        private String valueDeserializer;
        private String topicUserActions;
    }

    @Data
    public static class SimilarityConsumer {
        private String bootstrapServers;
        private String groupId;
        private String clientId;
        private boolean autoCommit;
        private String keyDeserializer;
        private String valueDeserializer;
        private String topicEventsSimilarity;
    }
}
