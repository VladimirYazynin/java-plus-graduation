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
        log.debug("Сохранение схожести события: {}", avro);
        EventSimilarityEntity existing = eventSimilarityRepository
                .findByEventAAndEventB(avro.getEventA(), avro.getEventB())
                .orElse(null);
        if (existing != null) {
            existing.setScore(avro.getScore());
            eventSimilarityRepository.save(existing);
        } else {
            eventSimilarityRepository.save(eventSimilarityMapper.toEventSimilarityEntity(avro));
        }
    }
}
