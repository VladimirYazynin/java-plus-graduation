package ru.practicum.stats.analyzer.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.stats.analyzer.entity.UserActionEntity;

@Mapper(componentModel = "spring")
public interface UserActionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "actionWeight", source = "actionWeight")
    @Mapping(target = "actionType", ignore = true)
    UserActionEntity toUserActionEntity(UserActionAvro userActionAvro, Double actionWeight);
}
