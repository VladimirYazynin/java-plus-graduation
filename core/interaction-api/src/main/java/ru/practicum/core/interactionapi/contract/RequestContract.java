package ru.practicum.core.interactionapi.contract;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.practicum.core.interactionapi.dto.EventRequestStatusUpdateRequest;
import ru.practicum.core.interactionapi.dto.EventRequestStatusUpdateResult;
import ru.practicum.core.interactionapi.dto.ParticipationRequestDto;

import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.CREATED;

public interface RequestContract {

    @ResponseStatus(CREATED)
    @PostMapping("/users/{userId}/requests")
    ParticipationRequestDto create(@PathVariable Long userId, @RequestParam Long eventId);

    @GetMapping("/users/{userId}/requests")
    List<ParticipationRequestDto> get(@PathVariable Long userId);

    @GetMapping("/internal/requests-by-eventId/{eventId}")
    List<ParticipationRequestDto> getAllByEventId(@PathVariable Long eventId);

    @GetMapping("/users/requests/confirmed")
    Map<Long, List<ParticipationRequestDto>> prepareConfirmedRequests(@RequestParam List<Long> eventIds);

    @PatchMapping("/internal/events/{eventId}/requests/status")
    EventRequestStatusUpdateResult updateRequestsStatus(@PathVariable Long eventId,
                                                        @RequestBody EventRequestStatusUpdateRequest request);

    @PatchMapping("/users/{userId}/requests/{requestId}/cancel")
    ParticipationRequestDto cancel(@PathVariable Long userId, @PathVariable Long requestId);

    @GetMapping("/requests/{userId}/{eventId}/confirmed")
    Boolean checkRegistration(@PathVariable Long eventId, @PathVariable Long userId);
}
