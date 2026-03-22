package ru.practicum.core.interactionapi.contract;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.practicum.core.interactionapi.dto.UserDto;
import ru.practicum.core.interactionapi.dto.UserShortDto;

import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

public interface UserContract {

    @PostMapping
    @ResponseStatus(CREATED)
    UserDto create(@Valid @RequestBody UserDto user);

    @GetMapping
    List<UserDto> get(@RequestParam(required = false) Long[] ids,
                             @RequestParam(defaultValue = "0") int from,
                             @RequestParam(defaultValue = "10") int size);

    @GetMapping("/{userId}")
    UserShortDto getUserById(@PathVariable("userId") @Positive Long userId);

    @DeleteMapping("/{userId}")
    @ResponseStatus(NO_CONTENT)
    void delete(@PathVariable Long userId);
}
