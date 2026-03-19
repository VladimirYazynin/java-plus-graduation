package ru.practicum.core.eventservice.compilation.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.core.eventservice.event.mapper.EventMapper;
import ru.practicum.core.eventservice.event.model.Event;
import ru.practicum.core.eventservice.event.repository.EventRepository;
import ru.practicum.core.interactionapi.dto.CompilationDto;
import ru.practicum.core.interactionapi.dto.NewCompilationDto;
import ru.practicum.core.eventservice.compilation.model.Compilation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CompilationMapper {
    private final EventRepository repository;
    private final EventMapper mapper;

    public Compilation toCompilation(NewCompilationDto compilation) {
        List<Event> events = new ArrayList<>();
        if (compilation.getEvents() != null) {
            events = repository.findAllById(compilation.getEvents());
        }
        return new Compilation(
                compilation.getId(),
                events,
                compilation.getPinned(),
                compilation.getTitle());
    }

    public CompilationDto toCompilationDto(Compilation compilation) {
        return new CompilationDto(
                compilation.getId(),
                compilation.getEvents().stream().map(mapper::toEventShortDto).collect(Collectors.toList()),
                compilation.getPinned(),
                compilation.getTitle());
    }
}
