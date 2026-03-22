package ru.practicum.core.interactionapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewEventDto {

    private Long id;

    @NotBlank
    @Size(min = 20, max = 2000)
    private String annotation;

    private Integer category;

    @NotBlank
    @Size(min = 20, max = 7000)
    private String description;
    private String eventDate;

    @NotNull
    private LocationDto location;
    private Boolean paid;
    private Integer participantLimit;
    private Boolean requestModeration;

    @Size(min = 3, max = 120)
    private String title;
}
