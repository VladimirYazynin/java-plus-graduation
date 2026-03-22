package ru.practicum.core.eventservice.compilation.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.core.eventservice.compilation.model.Compilation;
import ru.practicum.core.eventservice.event.model.Event;
import ru.practicum.core.interactionapi.dto.CompilationDto;
import ru.practicum.core.interactionapi.dto.EventShortDto;
import ru.practicum.core.interactionapi.dto.NewCompilationDto;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CompilationMapper {

    public Compilation toCompilation(NewCompilationDto compilation, List<Event> events) {
        return Compilation.builder()
                .events(events)
                .pinned(compilation.getPinned())
                .title(compilation.getTitle())
                .build();
    }

    public CompilationDto toCompilationDto(Compilation compilation, List<EventShortDto> eventsDto) {
        return new CompilationDto(
                compilation.getId(),
                eventsDto,
                compilation.getPinned(),
                compilation.getTitle());
    }
}
