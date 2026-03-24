package ru.practicum.core.eventservice.event.service.impl;

import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import ru.practicum.core.eventservice.category.model.Category;
import ru.practicum.core.eventservice.category.repository.CategoryRepository;
import ru.practicum.core.eventservice.client.CommentClient;
import ru.practicum.core.eventservice.client.RequestClient;
import ru.practicum.core.eventservice.client.UserClient;
import ru.practicum.core.eventservice.event.mapper.EventMapper;
import ru.practicum.core.eventservice.event.model.Event;
import ru.practicum.core.eventservice.event.model.Location;
import ru.practicum.core.eventservice.event.repository.EventRepository;
import ru.practicum.core.eventservice.event.repository.LocationRepository;
import ru.practicum.core.eventservice.event.service.EventService;
import ru.practicum.core.eventservice.event.service.StatClient;
import ru.practicum.core.interactionapi.dto.CommentShort;
import ru.practicum.core.interactionapi.dto.EventFullDto;
import ru.practicum.core.interactionapi.dto.EventRequestStatusUpdateRequest;
import ru.practicum.core.interactionapi.dto.EventRequestStatusUpdateResult;
import ru.practicum.core.interactionapi.dto.EventShortDto;
import ru.practicum.core.interactionapi.dto.NewEventDto;
import ru.practicum.core.interactionapi.dto.ParticipationRequestDto;
import ru.practicum.core.interactionapi.dto.UpdateEventAdminRequest;
import ru.practicum.core.interactionapi.dto.UpdateEventUserRequest;
import ru.practicum.core.interactionapi.dto.UserShortDto;
import ru.practicum.core.interactionapi.enums.State;
import ru.practicum.core.interactionapi.exception.AccessException;
import ru.practicum.core.interactionapi.exception.BadRequestException;
import ru.practicum.core.interactionapi.exception.DataViolationException;
import ru.practicum.core.interactionapi.exception.NotFoundException;
import ru.practicum.core.interactionapi.exception.ValidationException;
import ru.practicum.ewm.dto.EndpointHitDto;
import ru.practicum.ewm.dto.ViewStatsDto;
import ru.practicum.service.dashboard.RecommendedEventProto;
import ru.practicum.stats.statsclient.AnalyzerClient;
import ru.practicum.stats.statsclient.CollectorClient;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.practicum.core.interactionapi.enums.State.CANCELED;
import static ru.practicum.core.interactionapi.enums.State.CONFIRMED;
import static ru.practicum.core.interactionapi.enums.State.PENDING;
import static ru.practicum.core.interactionapi.enums.State.PUBLISHED;
import static ru.practicum.core.interactionapi.enums.State.PUBLISH_EVENT;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final LocationRepository locationRepository;
    private final EventMapper eventMapper;
    private final CategoryRepository categoryRepository;
    private final StatClient statClient;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final RequestClient requestClient;
    private final UserClient userClient;
    private final CommentClient commentClient;
    private final AnalyzerClient analyzerClient;
    private final CollectorClient collectorClient;

    @Override
    @Transactional
    public EventFullDto create(Long userId, NewEventDto event) {
        log.debug("create({}, {})", userId, event);
        UserShortDto user = userClient.getUserById(userId);
        Category category = categoryRepository.findById(event.getCategory())
                .orElseThrow(() -> new NotFoundException(
                        String.format("Не найдена категория с id: %d", event.getCategory())));
        Location location =
                locationRepository.findByLatAndLon(event.getLocation().getLat(), event.getLocation().getLon());
        if (location == null) {
            location = Location.builder()
                    .lat(event.getLocation().getLat())
                    .lon(event.getLocation().getLon())
                    .build();
            locationRepository.saveAndFlush(location);
        }
        Event thisEvent = eventMapper.toEvent(event, category, location);
        dateValidation(LocalDateTime.parse(event.getEventDate(), formatter));
        thisEvent.setPaid(event.getPaid() != null ? event.getPaid() : false);
        thisEvent.setParticipantLimit(
                Optional.ofNullable(event.getParticipantLimit())
                        .map(limit -> {
                            if (limit < 0) {
                                throw new ValidationException("Лимит участников не может быть отрицательным");
                            }
                            return limit;
                        })
                        .orElse(0)
        );
        thisEvent.setRequestModeration(event.getRequestModeration() != null ? event.getRequestModeration() : true);
        thisEvent.setCreatedOn(LocalDateTime.now());
        thisEvent.setInitiator(user.getId());
        thisEvent.setState(PENDING);
//        thisEvent.setViews(0L);
        thisEvent.setConfirmedRequests(0L);
        Event savedEvent = eventRepository.save(thisEvent);
        log.debug("Событие сохранено: {}", savedEvent);
        EventFullDto response = eventMapper.toEventFullDto(savedEvent, user, null);
        response.setRating(0d);
        return response;
    }

    @Override
    public List<EventFullDto> getAllByOwner(Long userId, int from, int size) {
        log.debug("getAllByOwner({}, {}, {})", userId, from, size);
        UserShortDto user = userClient.getUserById(userId);
        PageRequest page = PageRequest.of(from, size);
        List<EventFullDto> events = eventRepository.findAllByInitiator(user.getId(), page).stream()
                .map(e -> eventMapper.toEventFullDto(e, user, null))
                .toList();
        log.debug("Возвращён список событий по запросу пользователя: {}", events);
        return events;
    }

    @Override
    public EventFullDto getByIdByOwner(Long userId, Long eventId) {
        log.debug("getByIdByOwner({}, {})", userId, eventId);
        UserShortDto user = userClient.getUserById(userId);
        Event event = eventSearch(eventId);
        List<CommentShort> comments = commentClient.getCommentsForEvent(eventId);
        log.debug("Возвращено событие по запросу пользователя: {}", event);
        return eventMapper.toEventFullDto(event, user, comments);
    }

    @Override
    @Transactional
    public EventFullDto updateByIdByOwner(Long userId, Long eventId, UpdateEventUserRequest event) {
        log.debug("updateByIdByOwner({}, {}, {})", userId, eventId, event);
        UserShortDto user = userClient.getUserById(userId);
        Event thisEvent = eventSearch(eventId);
        if (thisEvent.getState().equals(PUBLISHED)) {
            throw new DataViolationException("Нельзя изменить данные");
        }
        if (event.getEventDate() != null) {
            dateValidation(LocalDateTime.parse(event.getEventDate(), formatter));
        }
        if (!thisEvent.getInitiator().equals(user.getId())) {
            throw new AccessException("Нет доступа");
        }
        if (event.getParticipantLimit() != null && event.getParticipantLimit() < 0) {
            throw new ValidationException("Лимит участников не может быть отрицательным");
        }
        Event eventToSave = fieldsChecker(event, thisEvent);
        if (event.getStateAction() != null) {
            switch (State.valueOf(event.getStateAction())) {
                case PUBLISH_EVENT:
                    thisEvent.setPublishedOn(LocalDateTime.now());
                    thisEvent.setState(PUBLISHED);
                    break;
                case REJECT_EVENT:
                case CANCEL_REVIEW:
                    thisEvent.setPublishedOn(LocalDateTime.now());
                    thisEvent.setState(CANCELED);
                    break;
                case SEND_TO_REVIEW:
                    thisEvent.setPublishedOn(LocalDateTime.now());
                    thisEvent.setState(PENDING);
                    break;
            }
        }
        Event updatedEvent = eventRepository.save(eventToSave);
        log.debug("Событие обновлено пользователем: {}", updatedEvent);
        List<CommentShort> comments = commentClient.getCommentsForEvent(updatedEvent.getId());
        return eventMapper.toEventFullDto(updatedEvent, user, comments);
    }

    @Override
    public List<ParticipationRequestDto> getRequests(Long userId, Long eventId) {
        log.debug("getRequests({}, {})", userId, eventId);
        UserShortDto user = userClient.getUserById(userId);
        Event event = eventSearch(eventId);
        if (!event.getInitiator().equals(user.getId())) {
            throw new AccessException("Нет доступа");
        }
        List<ParticipationRequestDto> requests = requestClient.getAllByEventId(eventId);
        log.debug("Возвращён список запросов по запросу пользователя: {}", requests);
        return requests;
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateStatus(Long userId,
                                                       Long eventId,
                                                       EventRequestStatusUpdateRequest request) {
        log.debug("updateStatus({}, {}, {})", userId, eventId, request);
        userClient.getUserById(userId);
        Event event = eventSearch(eventId);
        List<ParticipationRequestDto> requests = requestClient.getAllByEventId(eventId);
        List<Long> processingRequests = request.getRequestIds();
        if (request.getStatus().equals(CONFIRMED)) {
            long confirmed = requests.stream()
                    .filter(req -> State.valueOf(req.getStatus()).equals(CONFIRMED))
                    .count();
            if ((confirmed + processingRequests.size()) > event.getParticipantLimit()
                    || !event.getRequestModeration()) {
                throw new DataViolationException("Превышен лимит заявок");
            }
        }
        long confirmedRequests = requests.stream()
                .filter(req -> processingRequests.contains(req.getId())
                        && State.valueOf(req.getStatus()).equals(CONFIRMED))
                .count();
        if (confirmedRequests >= 1) {
            throw new DataViolationException("Нельзя изменить статус у уже подтверждённой заявки");
        }
        EventRequestStatusUpdateResult result =
                requestClient.updateRequestsStatus(eventId, request);
        log.debug("Статус запроса обновлён: {}", result);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventFullDto> searchEventsByAdmin(List<Long> userIds, List<String> states, List<Long> categoryIds,
                                                        LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size) {
        log.debug("Получен запрос на получения события по фильтрам с параметрами: " +
                        "userIds={}, states={}, categoryIds={}, rangeStart={}, rangeEnd={}, from={}, size={}",
                userIds, states, categoryIds, rangeStart, rangeEnd, from, size);
        if ((rangeStart != null) && (rangeEnd != null) && (rangeStart.isAfter(rangeEnd)))
            throw new BadRequestException("Время начала не может быть позже времени конца");
        List<State> eventStates = null;
        if (!CollectionUtils.isEmpty(states)) {
            try {
                eventStates = states.stream()
                        .map(String::toUpperCase)
                        .map(State::valueOf)
                        .collect(Collectors.toList());
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Передан неизвестный статус события: " + e.getMessage());
            }
        }
        List<Long> finalUserIds = CollectionUtils.isEmpty(userIds) ? null : userIds;
        List<State> finalEventStates = CollectionUtils.isEmpty(eventStates) ? null : eventStates;
        List<Long> finalCategoryIds = CollectionUtils.isEmpty(categoryIds) ? null : categoryIds;
        Pageable pageable = PageRequest.of(from / size, size);
        Page<Event> eventsPage = eventRepository.findAllByFiltersAdmin(
                finalUserIds,
                finalEventStates,
                finalCategoryIds,
                rangeStart,
                rangeEnd,
                pageable
        );
        List<Event> events = eventsPage.getContent();
        fillConfirmedRequestsInModels(events);
//        Map<Long, Long> views = getAmountOfViews(events);
        log.debug("Собираем событие для ответа");
        return events.stream()
                .map(event -> {
                    UserShortDto user = userClient.getUserById(event.getInitiator());
                    List<CommentShort> comments = commentClient.getCommentsForEvent(event.getId());
                    EventFullDto eventDto = eventMapper.toEventFullDto(event, user, comments);
//                    eventDto.setViews(views.getOrDefault(eventDto.getId(), 0L));
                    return eventDto;
                })
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private void fillConfirmedRequestsInModels(List<Event> events) {
        if (CollectionUtils.isEmpty(events)) return;
        List<Long> eventIds = events.stream()
                .map(Event::getId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (eventIds.isEmpty()) {
            events.forEach(e -> e.setConfirmedRequests(0L));
            return;
        }
        try {
            Map<Long, List<ParticipationRequestDto>> confirmedMap = requestClient.prepareConfirmedRequests(eventIds);
            events.forEach(event -> {
                List<ParticipationRequestDto> reqs = confirmedMap.getOrDefault(event.getId(), Collections.emptyList());
                event.setConfirmedRequests((long) reqs.size());
            });
        } catch (FeignException e) {
            log.warn("Не удалось заполнить confirmedRequests для eventIds {}: Fallback 0L", eventIds, e);
            events.forEach(event -> event.setConfirmedRequests(0L));
        }
    }

    private Map<Long, Long> getAmountOfViews(List<Event> events) {
        if (CollectionUtils.isEmpty(events)) {
            return Collections.emptyMap();
        }
        List<String> uris = events.stream()
                .map(event -> "/events/" + event.getId())
                .distinct()
                .collect(Collectors.toList());

        LocalDateTime startTime = LocalDateTime.now().minusDays(1);
        LocalDateTime endTime = LocalDateTime.now().plusMinutes(5);

        Map<Long, Long> viewsMap = new HashMap<>();
        try {
            log.debug("Получение статистики по времени для URI: {} с {} по {}", uris, startTime, endTime);
            List<ViewStatsDto> stats = statClient.receive(
                    startTime,
                    endTime,
                    uris,
                    true
            );
            log.debug("Получение статистики");
            if (!CollectionUtils.isEmpty(stats)) {
                for (ViewStatsDto stat : stats) {
                    Long eventId = Long.parseLong(stat.getUri().substring("/events/".length()));
                    viewsMap.put(eventId, stat.getHits());
                }
            }
        } catch (Exception e) {
            log.error("Не удалось получить статистику");
        }
        return viewsMap;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> searchEvents(String text, List<Long> categoryIds, Boolean paid, LocalDateTime rangeStart,
                                            LocalDateTime rangeEnd, Boolean onlyAvailable, String sort, Integer from,
                                            Integer size, HttpServletRequest request) {
        log.debug("Вызван метод getEventsWithFilters. Параметры: text='{}', categoryIds={}, paid={}, rangeStart={}, " +
                        "rangeEnd={}, onlyAvailable={}, sort='{}', from={}, size={}",
                text, categoryIds, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);
        if ((rangeStart != null) && (rangeEnd != null) && (rangeStart.isAfter(rangeEnd)))
            throw new BadRequestException("Время начала на может быть позже окончания");
        List<Event> events = eventRepository.findAllByFilters(
                text, categoryIds, paid, rangeStart, rangeEnd, onlyAvailable, PageRequest.of(from, size));
        fillConfirmedRequestsInModels(events);
//        Map<Long, Long> views = getAmountOfViews(events);
//        try {
//            statClient.post(EndpointHitDto.builder()
//                    .app("event-service")
//                    .uri(request.getRequestURI())
//                    .ip(request.getRemoteAddr())
//                    .timestamp(LocalDateTime.now().format(formatter))
//                    .build());
//        } catch (Exception e) {
//            log.error("Не удалось отправить запрос о сохранении на сервер статистики");
//        }
        log.debug("Собираем события для ответа");
        return events.stream().
                map(event -> {
                    UserShortDto user = userClient.getUserById(event.getInitiator());
                    EventShortDto dto = eventMapper.toEventShortDto(event, user);
//                    dto.setViews(views.getOrDefault(event.getId(), 0L));
                    return dto;
                })
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventFullDto> getRecommendation(Long userId, Integer max) {
        List<Long> eventIds = analyzerClient.getRecommendationsForUser(userId, max)
                .map(RecommendedEventProto::getEventId)
                .toList();
        List<Event> events = eventRepository.findAllByIdIn(eventIds);
        fillConfirmedRequestsInModels(events);
        return events.stream()
                .map(event -> {
                    EventFullDto eventDto = eventMapper.toEventFullDto(
                            event,
                            userClient.getUserById(event.getInitiator()),
                            null);
                    eventDto.setRating(analyzerClient.getInteractionsCount(List.of(event.getId()))
                            .map(RecommendedEventProto::getScore)
                            .findFirst()
                            .orElse(0.0));
                    return eventDto;
                })
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public EventFullDto getById(Long eventId, HttpServletRequest request, Long userId) {
        Event event = eventSearch(eventId);
        if (event.getState() != PUBLISHED) {
            throw new NotFoundException("Не найдено");
        }
//        sendData(request);
//        Event savedEvent = receiveData(event);
        collectorClient.collectUserAction(userId, eventId, "ACTION_VIEW", Instant.now());
        UserShortDto user = userClient.getUserById(event.getInitiator());
        List<CommentShort> comments = commentClient.getCommentsForEvent(event.getId());
        log.debug("Возвращено событие по запросу пользователя: {}", user);
        EventFullDto response = eventMapper.toEventFullDto(event, user, comments);
        response.setRating(analyzerClient.getInteractionsCount(List.of(event.getId()))
                .map(RecommendedEventProto::getScore)
                .findFirst()
                .orElse(0.0));
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public void addLike(Long eventId, Long userId) {
        eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Не найдено событие с id: %d", eventId)));
        if (requestClient.checkRegistration(eventId, userId)) {
            collectorClient.collectUserAction(userId, eventId, "ACTION_LIKE", Instant.now());
        } else {
            throw new NotFoundException("Пользователь не регистрировался на данное событие");
        }
    }

    @Override
    public EventFullDto getEventById(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Событие с id: %d не найдено", eventId)));
        UserShortDto user = userClient.getUserById(event.getInitiator());
        List<CommentShort> comments = commentClient.getCommentsForEvent(event.getId());
        return eventMapper.toEventFullDto(event, user, comments);
    }

    @Override
    @Transactional
    public EventFullDto updateByIdByAdmin(Long eventId, UpdateEventAdminRequest event) {
        log.info("updateByIdByAdmin({}, {})", eventId, event);
        Event thisEvent = eventSearch(eventId);
        if (!thisEvent.getState().equals(PENDING)) {
            throw new DataViolationException("Нельзя изменить данные события");
        }
        if (LocalDateTime.now().isAfter(thisEvent.getEventDate().minusHours(1))) {
            throw new ValidationException("Дата начала изменяемого события должна быть не ранее " +
                    "чем за час от даты публикации");
        }
        if (event.getEventDate() != null &&
                LocalDateTime.parse(event.getEventDate(), formatter).isBefore(LocalDateTime.now())) {
            throw new ValidationException("Дата не может быть в прошлом");
        }
        Event updatedEvent = fieldsChecker(eventMapper.toUpdateEventUserRequest(event), thisEvent);
        if (event.getStateAction() != null) {
            updatedEvent.setState(event.getStateAction().equals(PUBLISH_EVENT.toString()) ? PUBLISHED : CANCELED);
        }
        updatedEvent.setPublishedOn(LocalDateTime.now());
        Event savedEvent = eventRepository.save(updatedEvent);
        log.info("Событие обновлено администратором: {}", savedEvent);
        UserShortDto user = userClient.getUserById(savedEvent.getInitiator());
        List<CommentShort> comments = commentClient.getCommentsForEvent(savedEvent.getId());
        return eventMapper.toEventFullDto(savedEvent, user, comments);
    }

    private Event eventSearch(Long eventId) {
        log.info("eventSearch({})", eventId);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено"));
        log.info("Запрос на поиск события прошёл успешно: {}", event);
        return event;
    }

    private Event fieldsChecker(UpdateEventUserRequest eventToUpdate, Event foundEvent) {
        log.info("fieldsChecker({}, {})", eventToUpdate, foundEvent);
        if (eventToUpdate.getAnnotation() != null) {
            foundEvent.setAnnotation(eventToUpdate.getAnnotation());
        }
        if (eventToUpdate.getCategory() != null) {
            foundEvent.setCategory(categoryRepository.findById(eventToUpdate.getCategory())
                    .orElseThrow(() -> new NotFoundException("Категория не найдена")));
        }
        if (eventToUpdate.getDescription() != null) {
            foundEvent.setDescription(eventToUpdate.getDescription());
        }
        if (eventToUpdate.getLocation() != null) {
            Location location = Location.builder()
                    .lat(eventToUpdate.getLocation().getLat())
                    .lon(eventToUpdate.getLocation().getLon())
                    .build();
            foundEvent.setLocation(locationRepository.save(location));
        }
        if (eventToUpdate.getPaid() != null) {
            foundEvent.setPaid(eventToUpdate.getPaid());
        }
        if (eventToUpdate.getParticipantLimit() != null) {
            if (eventToUpdate.getParticipantLimit() < 0) {
                throw new ValidationException("Лимит участников не может быть отрицательным");
            }
            foundEvent.setParticipantLimit(eventToUpdate.getParticipantLimit());
        }
        if (eventToUpdate.getRequestModeration() != null) {
            foundEvent.setRequestModeration(eventToUpdate.getRequestModeration());
        }
        if (eventToUpdate.getTitle() != null) {
            foundEvent.setTitle(eventToUpdate.getTitle());
        }
        log.info("Валидация полей прошла успешно: {}", foundEvent);
        return foundEvent;
    }

    private void dateValidation(LocalDateTime date) {
        log.info("dateValidation({})", date);
        if (date.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ValidationException("Событие должно быть не меньше, чем за 2 часа до текущего времени");
        }
        log.info("Валидация даты прошла успешно: {}", date);
    }

    private void sendData(HttpServletRequest request) {
        log.info("sendData({})", request);
        EndpointHitDto endpointHit = new EndpointHitDto();
        endpointHit.setApp("event");
        endpointHit.setIp(request.getRemoteAddr());
        endpointHit.setUri(request.getRequestURI());
        endpointHit.setTimestamp(LocalDateTime.now().format(formatter));
        EndpointHitDto saved = statClient.post(endpointHit);
        log.info("Информация по запросу на endpoint '{}' успешно отправлена: {}", request.getRequestURI(), saved);
    }

    private Event receiveData(Event event) {
        log.info("receiveData({})", event);
        try {
            List<ViewStatsDto> viewStatsDto = statClient.receive(
                    LocalDateTime.now().minusYears(1),
                    LocalDateTime.now().plusDays(1),
                    List.of("/events/" + event.getId()),
                    true
            );
//            long views = 0L;
//            if (viewStatsDto != null && !viewStatsDto.isEmpty()) {
//                views = viewStatsDto.getFirst().getHits() != null ? viewStatsDto.getFirst().getHits() : 0L;
//            }
//            event.setViews(views);
            Event savedEvent = eventRepository.save(event);
            log.info("Сохранено событие с подсчётом просмотров: {}", savedEvent);
            return savedEvent;
        } catch (Exception e) {
            log.error("Ошибка при получении статистики для события {}", event.getId(), e);
            return event;
        }
    }
}
