package ru.practicum.core.eventservice.event.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.core.interactionapi.dto.EventFullDto;
import ru.practicum.core.interactionapi.dto.EventRequestStatusUpdateRequest;
import ru.practicum.core.interactionapi.dto.EventRequestStatusUpdateResult;
import ru.practicum.core.interactionapi.dto.EventShortDto;
import ru.practicum.core.interactionapi.dto.NewEventDto;
import ru.practicum.core.interactionapi.dto.ParticipationRequestDto;
import ru.practicum.core.interactionapi.dto.UpdateEventAdminRequest;
import ru.practicum.core.interactionapi.dto.UpdateEventUserRequest;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {

    EventFullDto create(Long userId, NewEventDto event);

    List<EventFullDto> getAllByOwner(Long userId, int from, int size);

    EventFullDto getByIdByOwner(Long userId, Long eventId);

    EventFullDto updateByIdByOwner(Long userId, Long eventId, UpdateEventUserRequest event);

    List<ParticipationRequestDto> getRequests(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateStatus(Long userId, Long eventId, EventRequestStatusUpdateRequest request);

    List<EventShortDto> searchEvents(String text, List<Long> categoryIds, Boolean paid, LocalDateTime rangeStart,
                                     LocalDateTime rangeEnd, Boolean onlyAvailable, String sort, Integer from,
                                     Integer size, HttpServletRequest request);

    EventFullDto getById(Long eventId, HttpServletRequest request);

    EventFullDto getEventById(Long eventId);

    EventFullDto updateByIdByAdmin(Long eventId, UpdateEventAdminRequest event);

    List<EventFullDto> searchEventsByAdmin(List<Long> userIds, List<String> states, List<Long> categoryIds,
                                           LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size);
}
