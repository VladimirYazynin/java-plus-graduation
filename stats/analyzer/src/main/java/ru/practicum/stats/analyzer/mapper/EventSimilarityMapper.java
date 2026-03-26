package ru.practicum.stats.analyzer.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.stats.analyzer.entity.EventSimilarityEntity;

@Mapper(componentModel = "spring")
public interface EventSimilarityMapper {

    @Mapping(target = "id", ignore = true)
    EventSimilarityEntity toEventSimilarityEntity(EventSimilarityAvro avro);
}
