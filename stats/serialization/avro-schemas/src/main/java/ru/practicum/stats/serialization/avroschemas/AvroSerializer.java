package ru.practicum.stats.serialization.avroschemas;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;

@Slf4j
public class AvroSerializer implements Serializer<SpecificRecordBase> {

    private BinaryEncoder binaryEncoder;
    private final EncoderFactory encoderFactory = EncoderFactory.get();

    public byte[] serialize(String topic, SpecificRecordBase data) {
        if (Objects.isNull(data)) {
            return null;
        }
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            DatumWriter<SpecificRecordBase> writer = new SpecificDatumWriter<>(data.getSchema());
            binaryEncoder = encoderFactory.binaryEncoder(out, binaryEncoder);
            writer.write(data, binaryEncoder);
            binaryEncoder.flush();
            return out.toByteArray();
        } catch (IOException e) {
            log.error("Произошла ошибка при сериализации: {}", e);
            throw new SerializationException(
                    String.format("Произошла ошибка во время сериализации данных для топика: %s", topic));
        }
    }
}
