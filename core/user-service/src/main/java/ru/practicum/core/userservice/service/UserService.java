package ru.practicum.core.userservice.service;

import ru.practicum.core.interactionapi.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto create(UserDto user);

    List<UserDto> get(Long[] ids, int from, int size);

    void delete(Long userId);
}
