package ru.practicum.core.interactionapi.dto;

import lombok.Data;
import ru.practicum.core.interactionapi.enums.State;
import ru.practicum.ewm.enums.State;

import java.util.List;

@Data
public class EventRequestStatusUpdateRequest {
    private List<Long> requestIds;
    private State status;
}
