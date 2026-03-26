package ru.practicum.core.requestservice.service;

import ru.practicum.core.interactionapi.dto.EventRequestStatusUpdateRequest;
import ru.practicum.core.interactionapi.dto.EventRequestStatusUpdateResult;
import ru.practicum.core.interactionapi.dto.ParticipationRequestDto;

import java.util.List;
import java.util.Map;

public interface RequestService {

    ParticipationRequestDto create(Long userId, Long eventId);

    List<ParticipationRequestDto> get(Long userId);

    List<ParticipationRequestDto> getAllByEventId(Long eventId);

    Map<Long, List<ParticipationRequestDto>> prepareConfirmedRequests(List<Long> eventIds);

    EventRequestStatusUpdateResult updateStatusByEvent(Long eventId, EventRequestStatusUpdateRequest request);

    ParticipationRequestDto cancel(Long userId, Long requestId);

    boolean checkRegistration(Long userId, Long eventId);
}
