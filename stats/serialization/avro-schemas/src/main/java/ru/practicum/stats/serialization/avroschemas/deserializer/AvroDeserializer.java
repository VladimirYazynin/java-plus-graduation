package ru.practicum.stats.serialization.avroschemas.deserializer;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

@Slf4j
public class AvroDeserializer<T extends SpecificRecordBase> implements Deserializer<T> {

    private final DatumReader<T> datumReader;
    private final DecoderFactory decoderFactory;

    public AvroDeserializer(Schema schema) {
        this(DecoderFactory.get(), schema);
    }

    public AvroDeserializer(DecoderFactory decoderFactory, Schema schema) {
        this.decoderFactory = decoderFactory;
        this.datumReader = new SpecificDatumReader<>(schema);
    }

    @Override
    public T deserialize(String topic, byte[] data) {
        try {
            if (data != null) {
                BinaryDecoder decoder = decoderFactory.binaryDecoder(data, null);
                return this.datumReader.read(null, decoder);
            }
            return null;
        } catch (Exception e) {
            log.error("Произошла ошибка во время десериализации: {}", e);
            throw new SerializationException(
                    String.format("Произошла ошибка во время десериализации данных из топика: &s", topic));
        }
    }
}
