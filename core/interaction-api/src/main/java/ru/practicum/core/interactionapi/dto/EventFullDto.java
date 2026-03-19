package ru.practicum.core.interactionapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.core.interactionapi.enums.State;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.comment.dto.CommentShort;
import ru.practicum.ewm.enums.State;
import ru.practicum.core.eventservice.event.model.Location;
import ru.practicum.ewm.user.dto.UserShortDto;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventFullDto {
    private Long id;
    private String annotation;
    private CategoryDto category;
    private String description;
    private String eventDate;
    private Location location;
    private Boolean paid;
    private Integer participantLimit;
    private Boolean requestModeration;
    private String title;
    private Integer confirmedRequests;
    private String createdOn;
    private UserShortDto initiator;
    private String publishedOn;
    private State state;
    private Integer views;
    private List<CommentShort> comments;
}
