package ru.practicum.core.eventservice.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.core.interactionapi.contract.EventContract;
import ru.practicum.core.interactionapi.dto.EventFullDto;
import ru.practicum.core.interactionapi.dto.EventRequestStatusUpdateRequest;
import ru.practicum.core.interactionapi.dto.EventRequestStatusUpdateResult;
import ru.practicum.core.interactionapi.dto.EventShortDto;
import ru.practicum.core.interactionapi.dto.NewEventDto;
import ru.practicum.core.interactionapi.dto.ParticipationRequestDto;
import ru.practicum.core.interactionapi.dto.UpdateEventAdminRequest;
import ru.practicum.core.interactionapi.dto.UpdateEventUserRequest;
import ru.practicum.core.eventservice.event.service.EventService;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;

@Slf4j
@RestController
@RequestMapping
@RequiredArgsConstructor
public class EventController implements EventContract {

    private final EventService eventService;

    @Override
    @ResponseStatus(CREATED)
    @PostMapping("/users/{userId}/events")
    public EventFullDto create(@PathVariable Long userId, @Valid @RequestBody NewEventDto event) {
        return eventService.create(userId, event);
    }

    @Override
    @GetMapping("/users/{userId}/events")
    public List<EventFullDto> getAllByOwner(@PathVariable Long userId,
                                            @RequestParam(defaultValue = "0") int from,
                                            @RequestParam(defaultValue = "10") int size) {
        return eventService.getAllByOwner(userId, from, size);
    }

    @Override
    @GetMapping("/users/{userId}/events/{eventId}")
    public EventFullDto getByIdByOwner(@PathVariable Long userId, @PathVariable Long eventId) {
        return eventService.getByIdByOwner(userId, eventId);
    }

    @Override
    @PatchMapping("/users/{userId}/events/{eventId}")
    public EventFullDto updateByIdByOwner(@PathVariable Long userId,
                                          @PathVariable Long eventId,
                                          @Valid @RequestBody UpdateEventUserRequest event) {
        return eventService.updateByIdByOwner(userId, eventId, event);
    }

    @Override
    @GetMapping("/users/{userId}/events/{eventId}/requests")
    public List<ParticipationRequestDto> getRequestsByOwner(@PathVariable Long userId,
                                                            @PathVariable Long eventId) {
        return eventService.getRequests(userId, eventId);
    }

    @Override
    @PatchMapping("/users/{userId}/events/{eventId}/requests")
    public EventRequestStatusUpdateResult updateStatus(@PathVariable Long userId,
                                                       @PathVariable Long eventId,
                                                       @RequestBody EventRequestStatusUpdateRequest request) {
        return eventService.updateStatus(userId, eventId, request);
    }

    @Override
    @GetMapping("/events")
    public List<EventShortDto> searchEvents(@RequestParam(required = false) String text,
                                            @RequestParam(required = false) List<Long> categories,
                                            @RequestParam(required = false) Boolean paid,
                                            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                            @RequestParam(defaultValue = "true", required = false) Boolean onlyAvailable,
                                            @RequestParam(required = false, defaultValue = "EVENT_DATE") String sort,
                                            @RequestParam(required = false, defaultValue = "0") int from,
                                            @RequestParam(required = false, defaultValue = "10") int size,
                                            HttpServletRequest request) {
        return eventService.searchEvents(text, categories, paid, rangeStart, rangeEnd,
                onlyAvailable, sort, from, size, request);
    }

    @Override
    @GetMapping("/events/{eventId}")
    public EventFullDto getById(@PathVariable Long eventId,
                                HttpServletRequest request,
                                @RequestHeader("X-EWM-USER-ID") Long userId) {
        return eventService.getById(eventId, request, userId);
    }

    @GetMapping("/events/recommendations")
    public List<EventFullDto> getRecommendations(@RequestHeader("X-EWM-USER-ID") Long userId, Integer max) {
        return eventService.getRecommendation(userId, max);
    }

    @PutMapping("/events/{eventId}/like")
    public void addLike(@PathVariable Long eventId, @RequestHeader("X-EWM-USER-ID") Long userId) {
        log.debug("Поступил запрос на лайк для события");
        eventService.addLike(eventId, userId);
    }

    @Override
    @GetMapping("/internal/events/{eventId}")
    public EventFullDto getEventById(@PathVariable Long eventId) {
        return eventService.getEventById(eventId);
    }

    @Override
    @GetMapping("/admin/events")
    public List<EventFullDto> search(@RequestParam(required = false) List<Long> users,
                                     @RequestParam(required = false) List<String> states,
                                     @RequestParam(required = false) List<Long> categories,
                                     @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                         LocalDateTime rangeStart,
                                     @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                          LocalDateTime rangeEnd,
                                     @RequestParam(required = false, defaultValue = "0") int from,
                                     @RequestParam(required = false, defaultValue = "10") int size) {
        return eventService.searchEventsByAdmin(users, states, categories, rangeStart, rangeEnd, from, size);
    }

    @Override
    @PatchMapping("/admin/events/{eventId}")
    public EventFullDto adminVerification(@PathVariable Long eventId,
                                          @Valid @RequestBody UpdateEventAdminRequest event) {
        return eventService.updateByIdByAdmin(eventId, event);
    }
}
