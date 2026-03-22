package ru.practicum.core.requestservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.core.interactionapi.dto.*;
import ru.practicum.core.interactionapi.enums.State;
import ru.practicum.core.requestservice.client.EventClient;
import ru.practicum.core.requestservice.client.UserClient;
import ru.practicum.core.interactionapi.exception.AccessException;
import ru.practicum.core.interactionapi.exception.DataViolationException;
import ru.practicum.core.interactionapi.exception.NotFoundException;
import ru.practicum.core.requestservice.mapper.RequestMapper;
import ru.practicum.core.requestservice.model.ParticipationRequest;
import ru.practicum.core.requestservice.repository.RequestRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ru.practicum.core.interactionapi.enums.State.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final RequestMapper requestMapper;
    private final EventClient eventClient;
    private final UserClient userClient;

    @Override
    @Transactional
    public ParticipationRequestDto create(Long userId, Long eventId) {
        log.info("create({}, {})", userId, eventId);
        UserShortDto user = userClient.getUserById(userId);
        EventFullDto event = eventClient.getEventById(eventId);
        if (!event.getState().equals(PUBLISHED)) {
            throw new DataViolationException("Событие не найдено");
        }
        if (requestRepository.existsByEventAndRequester(event.getId(), user.getId())) {
            throw new DataViolationException("Нельзя отправить запрос повторно");
        }
        if (event.getInitiator().equals(user)) {
            throw new DataViolationException("Вы уже участвуете в событии, будучи организатором");
        }
        if (!event.getRequestModeration()
                && event.getParticipantLimit() == requestRepository.findByEvent(eventId).size()) {
            throw new DataViolationException("Нет мест для участия в мероприятии");
        }
        ParticipationRequest request = new ParticipationRequest();
        request.setCreated(LocalDateTime.now());
        request.setEvent(event.getId());
        request.setRequester(user.getId());
        State status = event.getRequestModeration() ? PENDING : CONFIRMED;
        request.setStatus(event.getParticipantLimit() == 0 ? CONFIRMED : status);
        ParticipationRequest savedRequest = requestRepository.save(request);
        log.info("Запрос создан: {}", savedRequest);
        return requestMapper.toParticipationRequestDto(savedRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> get(Long userId) {
        log.info("get({})", userId);
        UserShortDto user = userClient.getUserById(userId);
        List<ParticipationRequestDto> requests = requestRepository.findAllByRequester(user.getId())
                .stream().map(requestMapper::toParticipationRequestDto).collect(Collectors.toList());
        log.info("По запросу пользователя возвращён список заявок: {}", requests);
        return requests;
    }

    @Override
    public List<ParticipationRequestDto> getAllByEventId(Long eventId) {
        return requestRepository.findByEvent(eventId).stream()
                .map(requestMapper::toParticipationRequestDto)
                .toList();
    }

    @Override
    public Map<Long, List<ParticipationRequestDto>> prepareConfirmedRequests(List<Long> eventIds) {
        log.info("Находим список подтверждённых запросов для переданных id событий");
        List<ParticipationRequest> requests = requestRepository.findConfirmedRequestsByEventIds(eventIds, State.CONFIRMED);
        List<ParticipationRequestDto> confirmedRequests = requests.stream()
                .map(requestMapper::toParticipationRequestDto)
                .toList();
        Map<Long, List<ParticipationRequestDto>> result = new HashMap<>();
        for (ParticipationRequestDto request : confirmedRequests) {
            Long eventId = request.getEvent();
            List<ParticipationRequestDto> list = result.get(eventId);
            if (list == null) {
                list = new ArrayList<>();
            }
            list.add(request);
            result.put(eventId, list);
        }
        return result;
    }

    @Transactional
    public EventRequestStatusUpdateResult updateStatusByEvent(Long eventId, EventRequestStatusUpdateRequest request) {
        List<ParticipationRequest> requests =
                requestRepository.findAllByEventAndIdIn(eventId, request.getRequestIds());
        requests.forEach(r -> r.setStatus(request.getStatus()));
        requestRepository.saveAll(requests);
        List<ParticipationRequestDto> dtos = requests.stream()
                .map(requestMapper::toParticipationRequestDto)
                .toList();
        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();
        if (request.getStatus().equals(REJECTED)) {
            result.setRejectedRequests(dtos);
        } else {
            result.setConfirmedRequests(dtos);
        }
        return result;
    }


    @Override
    @Transactional
    public ParticipationRequestDto cancel(Long userId, Long requestId) {
        log.info("cancel({}, {})", userId, requestId);
        UserShortDto user = userClient.getUserById(userId);
        ParticipationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Данные не найдены"));
        if (!request.getRequester().equals(user.getId())) {
            throw new AccessException("Нет доступа");
        }
        request.setStatus(CANCELED);
        ParticipationRequest savedRequest = requestRepository.save(request);
        log.info("Заявка на участие в событии отменена: {}", savedRequest);
        return requestMapper.toParticipationRequestDto(savedRequest);
    }
}
