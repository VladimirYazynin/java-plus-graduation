package ru.practicum.core.requestservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.core.interactionapi.contract.RequestContract;
import ru.practicum.core.interactionapi.dto.EventRequestStatusUpdateRequest;
import ru.practicum.core.interactionapi.dto.EventRequestStatusUpdateResult;
import ru.practicum.core.interactionapi.dto.ParticipationRequestDto;
import ru.practicum.core.requestservice.service.RequestService;

import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.CREATED;

@Slf4j
@RestController
@RequiredArgsConstructor
public class RequestController implements RequestContract {

    private final RequestService requestService;

    @Override
    @PostMapping("/users/{userId}/requests")
    @ResponseStatus(CREATED)
    public ParticipationRequestDto create(@PathVariable Long userId, @RequestParam Long eventId) {
        return requestService.create(userId, eventId);
    }

    @Override
    @GetMapping("/users/{userId}/requests")
    public List<ParticipationRequestDto> get(@PathVariable Long userId) {
        return requestService.get(userId);
    }

    @Override
    @GetMapping("/internal/requests-by-eventId/{eventId}")
    public List<ParticipationRequestDto> getAllByEventId(@PathVariable Long eventId) {
        return requestService.getAllByEventId(eventId);
    }

    @Override
    @PatchMapping("/internal/events/{eventId}/requests/status")
    public EventRequestStatusUpdateResult updateRequestsStatus(@PathVariable Long eventId,
                                                               @RequestBody EventRequestStatusUpdateRequest request) {
        log.debug("Массовое обновление статусов заявок по eventId={}, body={}", eventId, request);
        return requestService.updateStatusByEvent(eventId, request);
    }

    @Override
    @GetMapping("/users/requests/confirmed")
    public Map<Long, List<ParticipationRequestDto>> prepareConfirmedRequests(@RequestParam List<Long> eventIds) {
        log.info("Получен список eventIds {} для получения подтверждённых заявок", eventIds);
        return requestService.prepareConfirmedRequests(eventIds);
    }

    @Override
    @PatchMapping("/users/{userId}/requests/{requestId}/cancel")
    public ParticipationRequestDto cancel(@PathVariable Long userId, @PathVariable Long requestId) {
        return requestService.cancel(userId, requestId);
    }

    @Override
    @GetMapping("/requests/{userId}/{eventId}/confirmed")
    public Boolean checkRegistration(@PathVariable Long eventId, @PathVariable Long userId) {
        return requestService.checkRegistration(eventId, userId);
    }
}
