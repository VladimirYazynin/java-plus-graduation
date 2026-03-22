package ru.practicum.core.eventservice.compilation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.core.eventservice.client.UserClient;
import ru.practicum.core.eventservice.event.mapper.EventMapper;
import ru.practicum.core.eventservice.event.model.Event;
import ru.practicum.core.eventservice.event.repository.EventRepository;
import ru.practicum.core.eventservice.compilation.mapper.CompilationMapper;
import ru.practicum.core.eventservice.compilation.model.Compilation;
import ru.practicum.core.eventservice.compilation.repository.CompilationRepository;
import ru.practicum.core.interactionapi.dto.CompilationDto;
import ru.practicum.core.interactionapi.dto.EventShortDto;
import ru.practicum.core.interactionapi.dto.NewCompilationDto;
import ru.practicum.core.interactionapi.dto.UpdateCompilationDto;
import ru.practicum.core.interactionapi.dto.UserShortDto;
import ru.practicum.core.interactionapi.exception.BadRequestException;
import ru.practicum.core.interactionapi.exception.NotFoundException;
import ru.practicum.core.interactionapi.exception.ValidationException;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final UserClient  userClient;
    private final CompilationMapper mapper;
    private final CompilationMapper compilationMapper;

    @Override
    @Transactional
    public CompilationDto create(NewCompilationDto compilation) {
        log.info("create({})", compilation);
        List<Long> eventIds = compilation.getEvents();
        List<Event> events = List.of();
        if (eventIds != null && !eventIds.isEmpty()) {
            if (eventIds.stream().anyMatch(Objects::isNull)) {
                throw new BadRequestException("Идентификаторы событий не могут быть null");
            }
            events = eventRepository.findAllById(eventIds);
        }
        Compilation thisCompilation = mapper.toCompilation(compilation, events);
        titleValidation(compilation.getTitle());
        thisCompilation.setPinned(
                compilation.getPinned() != null ? compilation.getPinned() : false
        );
        Compilation saved = compilationRepository.save(thisCompilation);
        log.info("Добавлена подборка: {}", saved);
        List<EventShortDto> eventsDto = saved.getEvents().stream()
                .map(event -> {
                    UserShortDto user = userClient.getUserById(event.getInitiator());
                    return eventMapper.toEventShortDto(event, user);
                })
                .toList();
        return mapper.toCompilationDto(saved, eventsDto);
    }


    @Override
    @Transactional
    public CompilationDto update(Long compId, UpdateCompilationDto compilation) {
        log.info("update({}, {})", compId, compilation);
        Compilation foundCompilation = findById(compId);
        titleValidation(compilation.getTitle());
        if (compilation.getTitle() != null) {
            foundCompilation.setTitle(compilation.getTitle());
        }
        if (compilation.getEvents() != null) {
            List<Long> eventsIds = compilation.getEvents();
            List<Event> events = eventRepository.findAllById(Stream.concat(eventsIds.stream(),
                            foundCompilation.getEvents().stream()
                                    .map(Event::getId).toList().stream())
                    .filter(eventsIds::contains).collect(Collectors.toList()));
            foundCompilation.setEvents(events);
        }
        if (compilation.getPinned() != null) {
            foundCompilation.setPinned(compilation.getPinned());
        }
        Compilation saved = compilationRepository.save(foundCompilation);
        log.info("Подборка обновлена: {}", saved);
        List<EventShortDto> eventsDto = saved.getEvents().stream()
                .map(event -> {
                    UserShortDto user = userClient.getUserById(event.getInitiator());
                    return eventMapper.toEventShortDto(event, user);
                })
                .toList();
        return mapper.toCompilationDto(saved, eventsDto);
    }

    @Override
    @Transactional
    public void delete(Long compId) {
        log.info("delete({})", compId);
        Compilation compilation = findById(compId);
        compilationRepository.delete(compilation);
        log.info("Подборка удалена: {}", compilation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompilationDto> getAll(Boolean pinned, int from, int size) {
        log.info("getAll({}, {}, {})", pinned, from, size);
        PageRequest page = PageRequest.of(from, size);
        List<CompilationDto> compilations;
        if (pinned != null && pinned) {
            compilations = compilationRepository.findAllByPinned(pinned, page).stream()
                    .map(compilation -> {
                        List<EventShortDto> eventsDto = compilation.getEvents().stream()
                                .map(event -> {
                                    UserShortDto user = userClient.getUserById(event.getInitiator());
                                    return eventMapper.toEventShortDto(event, user);
                                })
                                .toList();
                        return compilationMapper.toCompilationDto(compilation, eventsDto);
                    }).toList();
        } else {
            compilations = compilationRepository.findAll(page).stream()
                    .map(compilation -> {
                        List<EventShortDto> eventsDto = compilation.getEvents().stream()
                                .map(event -> {
                                    UserShortDto user = userClient.getUserById(event.getInitiator());
                                    return eventMapper.toEventShortDto(event, user);
                                })
                                .toList();
                        return compilationMapper.toCompilationDto(compilation, eventsDto);
                    })
                    .toList();
        }
        log.info("Возвращён список с подборками: {}", compilations);
        return compilations;
    }

    @Override
    @Transactional(readOnly = true)
    public CompilationDto getById(Long compId) {
        log.info("getById({})", compId);
        Compilation compilation = findById(compId);
        log.info("Возвращена подборка по запросу пользователя: {}", compilation);
        List<EventShortDto> eventsDto = compilation.getEvents().stream()
                .map(event -> {
                    UserShortDto user = userClient.getUserById(event.getInitiator());
                    return eventMapper.toEventShortDto(event, user);
                })
                .toList();
        return mapper.toCompilationDto(compilation, eventsDto);
    }

    private Compilation findById(Long compId) {
        log.info("findById({})", compId);
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Подборка не найдена"));
        log.info("Подборка найдена: {}", compilation);
        return compilation;
    }

    private void titleValidation(String title) {
        if (compilationRepository.existsByTitle(title)) {
            throw new ValidationException("Такая подборка уже существует");
        }
    }
}
