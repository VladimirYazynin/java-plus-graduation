package ru.practicum.core.interactionapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.core.interactionapi.enums.State;

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
    private LocationDto location;
    private Boolean paid;
    private Integer participantLimit;
    private Boolean requestModeration;
    private String title;
    private Long confirmedRequests;
    private String createdOn;
    private UserShortDto initiator;
    private String publishedOn;
    private State state;
    private Double rating;
    private List<CommentShort> comments;
}
