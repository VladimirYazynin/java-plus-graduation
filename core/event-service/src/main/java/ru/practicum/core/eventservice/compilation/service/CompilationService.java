package ru.practicum.core.eventservice.compilation.service;

import ru.practicum.core.interactionapi.dto.CompilationDto;
import ru.practicum.core.interactionapi.dto.NewCompilationDto;
import ru.practicum.core.interactionapi.dto.UpdateCompilationDto;

import java.util.List;

public interface CompilationService {
    CompilationDto create(NewCompilationDto compilation);

    CompilationDto update(Long compId, UpdateCompilationDto compilation);

    void delete(Long compId);

    List<CompilationDto> getAll(Boolean pinned, int from, int size);

    CompilationDto getById(Long compId);
}
