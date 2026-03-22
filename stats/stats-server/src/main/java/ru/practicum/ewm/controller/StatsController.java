package ru.practicum.ewm.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.contract.StatContract;
import ru.practicum.ewm.dto.EndpointHitDto;
import ru.practicum.ewm.dto.ViewStatsDto;
import ru.practicum.ewm.service.StatsService;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequiredArgsConstructor
public class StatsController implements StatContract {

    private final StatsService service;
    private static final String PATTERN = "yyyy-MM-dd HH:mm:ss";

    @Override
    @PostMapping("/hit")
    @ResponseStatus(CREATED)
    public EndpointHitDto post(@RequestBody EndpointHitDto endpointHit) {
        return service.send(endpointHit);
    }

    @Override
    @GetMapping("/stats")
    public List<ViewStatsDto> receive(@RequestParam @DateTimeFormat(pattern = PATTERN) LocalDateTime start,
                                      @RequestParam @DateTimeFormat(pattern = PATTERN) LocalDateTime end,
                                      @RequestParam(required = false) List<String> uris,
                                      @RequestParam(defaultValue = "false") Boolean unique) {
        return service.receive(start, end, uris, unique);
    }
}
