package ru.practicum.core.userservice.service;

import ru.practicum.core.interactionapi.dto.UserDto;
import ru.practicum.core.interactionapi.dto.UserShortDto;

import java.util.List;

public interface UserService {

    UserDto create(UserDto user);

    List<UserDto> get(Long[] ids, int from, int size);

    UserShortDto getUserById(Long userId);

    void delete(Long userId);
}
