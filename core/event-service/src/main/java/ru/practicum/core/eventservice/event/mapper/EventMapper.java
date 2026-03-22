package ru.practicum.core.eventservice.event.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.core.eventservice.category.mapper.CategoryMapper;
import ru.practicum.core.eventservice.category.model.Category;
import ru.practicum.core.eventservice.event.model.Location;
import ru.practicum.core.eventservice.event.model.Event;
import ru.practicum.core.interactionapi.dto.CommentShort;
import ru.practicum.core.interactionapi.dto.EventFullDto;
import ru.practicum.core.interactionapi.dto.EventShortDto;
import ru.practicum.core.interactionapi.dto.LocationDto;
import ru.practicum.core.interactionapi.dto.NewEventDto;
import ru.practicum.core.interactionapi.dto.UpdateEventAdminRequest;
import ru.practicum.core.interactionapi.dto.UpdateEventUserRequest;
import ru.practicum.core.interactionapi.dto.UserShortDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
public class EventMapper {

    private final CategoryMapper categoryMapper;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Event toEvent(NewEventDto event, Category category, Location location) {
        return new Event(
                event.getId(),
                event.getAnnotation(),
                category,
                event.getDescription(),
                LocalDateTime.parse(event.getEventDate(), formatter),
                location,
                event.getPaid(),
                event.getParticipantLimit(),
                event.getRequestModeration(),
                event.getTitle());
    }

    public EventFullDto toEventFullDto(Event event, UserShortDto user, List<CommentShort> comments) {
        String publishedOn = null;
        if (event.getPublishedOn() != null) {
            publishedOn = event.getPublishedOn().format(formatter);
        }
        return new EventFullDto(
                event.getId(),
                event.getAnnotation(),
                categoryMapper.toCategoryDto(event.getCategory()),
                event.getDescription(),
                event.getEventDate().format(formatter),
                new LocationDto(event.getLocation().getLat(), event.getLocation().getLon()),
                event.getPaid(),
                event.getParticipantLimit(),
                event.getRequestModeration(),
                event.getTitle(),
                event.getConfirmedRequests(),
                event.getCreatedOn().format(formatter),
                user,
                publishedOn,
                event.getState(),
                event.getViews(),
                comments
        );
    }

    public EventShortDto toEventShortDto(Event event, UserShortDto user) {
        return new EventShortDto(
                event.getId(),
                event.getAnnotation(),
                categoryMapper.toCategoryDto(event.getCategory()),
                event.getConfirmedRequests(),
                event.getEventDate().format(formatter),
                user,
                event.getPaid(),
                event.getTitle(),
                event.getViews());
    }

    public UpdateEventUserRequest toUpdateEventUserRequest(UpdateEventAdminRequest event) {
        return new UpdateEventUserRequest(
                event.getId(),
                event.getAnnotation(),
                event.getCategory(),
                event.getDescription(),
                event.getEventDate(),
                event.getLocation(),
                event.getPaid(),
                event.getParticipantLimit(),
                event.getRequestModeration(),
                event.getStateAction(),
                event.getTitle());
    }
}
