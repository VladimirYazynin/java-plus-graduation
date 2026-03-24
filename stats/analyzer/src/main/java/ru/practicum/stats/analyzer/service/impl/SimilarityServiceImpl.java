package ru.practicum.stats.analyzer.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.stats.analyzer.entity.EventSimilarityEntity;
import ru.practicum.stats.analyzer.mapper.EventSimilarityMapper;
import ru.practicum.stats.analyzer.repository.EventSimilarityRepository;
import ru.practicum.stats.analyzer.service.SimilarityService;

@Slf4j
@Service
@RequiredArgsConstructor
public class SimilarityServiceImpl implements SimilarityService {

    private final EventSimilarityRepository eventSimilarityRepository;
    private final EventSimilarityMapper eventSimilarityMapper;

    @Override
    public void handleSimilarity(EventSimilarityAvro avro) {
        log.debug("Создание схожести события: {}", avro);
        if (eventSimilarityRepository.existsByEventAAndEventB(avro.getEventA(), avro.getEventB())) {
            log.debug("Запись с eventA: {} и eventB: {} уже есть", avro.getEventA(), avro.getEventB());
            return;
        }
        EventSimilarityEntity similarity = eventSimilarityMapper.toEventSimilarityEntity(avro);
        eventSimilarityRepository.save(similarity);
    }
}
