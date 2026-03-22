package ru.practicum.core.interactionapi.contract;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
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

import static org.springframework.http.HttpStatus.CREATED;

public interface EventContract {

    @PostMapping("/users/{userId}/events")
    @ResponseStatus(CREATED)
    EventFullDto create(@PathVariable Long userId, @Valid @RequestBody NewEventDto event);

    @GetMapping("/users/{userId}/events")
    List<EventFullDto> getAllByOwner(@PathVariable Long userId,
                                     @RequestParam(defaultValue = "0") int from,
                                     @RequestParam(defaultValue = "10") int size);

    @GetMapping("/users/{userId}/events/{eventId}")
    EventFullDto getByIdByOwner(@PathVariable Long userId, @PathVariable Long eventId);

    @PatchMapping("/users/{userId}/events/{eventId}")
    EventFullDto updateByIdByOwner(@PathVariable Long userId,
                                   @PathVariable Long eventId,
                                   @Valid @RequestBody UpdateEventUserRequest event);

    @GetMapping("/users/{userId}/events/{eventId}/requests")
    List<ParticipationRequestDto> getRequestsByOwner(@PathVariable Long userId,
                                                     @PathVariable Long eventId);

    @PatchMapping("/users/{userId}/events/{eventId}/requests")
    EventRequestStatusUpdateResult updateStatus(@PathVariable Long userId,
                                                @PathVariable Long eventId,
                                                @RequestBody EventRequestStatusUpdateRequest request);

    @GetMapping("/events")
    List<EventShortDto> searchEvents(@RequestParam(required = false) String text,
                                     @RequestParam(required = false) List<Long> categories,
                                     @RequestParam(required = false) Boolean paid,
                                     @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                     @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                     @RequestParam(defaultValue = "true", required = false) Boolean onlyAvailable,
                                     @RequestParam(required = false, defaultValue = "EVENT_DATE") String sort,
                                     @RequestParam(required = false, defaultValue = "0") int from,
                                     @RequestParam(required = false, defaultValue = "10") int size,
                                     HttpServletRequest request);

    @GetMapping("/events/{eventId}")
    EventFullDto getById(@PathVariable Long eventId, HttpServletRequest request);

    @GetMapping("/internal/events/{eventId}")
    EventFullDto getEventById(@PathVariable Long eventId);

    @GetMapping("/admin/events")
    List<EventFullDto> search(@RequestParam(required = false) List<Long> users,
                              @RequestParam(required = false) List<String> states,
                              @RequestParam(required = false) List<Long> categories,
                              @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                              LocalDateTime rangeStart,
                              @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                              LocalDateTime rangeEnd,
                              @RequestParam(required = false, defaultValue = "0") int from,
                              @RequestParam(required = false, defaultValue = "10") int size);

    @PatchMapping("/admin/events/{eventId}")
    EventFullDto adminVerification(@PathVariable Long eventId, @Valid @RequestBody UpdateEventAdminRequest event);
}
