package ru.practicum.core.userservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.core.interactionapi.dto.UserShortDto;
import ru.practicum.core.interactionapi.exception.DataViolationException;
import ru.practicum.core.interactionapi.exception.DuplicatedDataException;
import ru.practicum.core.interactionapi.exception.NotFoundException;
import ru.practicum.core.interactionapi.dto.UserDto;
import ru.practicum.core.userservice.mapper.UserMapper;
import ru.practicum.core.userservice.model.User;
import ru.practicum.core.userservice.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository repository;
    private final UserMapper mapper;

    @Override
    @Transactional
    public UserDto create(UserDto user) {
        log.info("create({})", user);
        validateEmailExist(user.getEmail());
        User thisUser = mapper.toUser(user);
        if (repository.existsByName(thisUser.getName())) {
            throw new DataViolationException("Пользователь уже существует");
        }
        User savedUser = repository.save(thisUser);
        log.info("Пользователь сохранён: {}", savedUser);
        return mapper.toUserDto(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> get(Long[] ids, int from, int size) {
        log.info("get({}, {}, {})", ids, from, size);
        PageRequest page = PageRequest.of(from, size);
        List<UserDto> users;
        if (ids != null) {
            users = repository.findAllByIdIn(List.of(ids), page).stream().map(mapper::toUserDto)
                    .collect(Collectors.toList());
        } else {
            users = repository.findAll(page).stream().map(mapper::toUserDto).collect(Collectors.toList());
        }
        log.info("Возвращён список пользователей по запросу администратора: {}", users);
        return users;
    }

    @Override
    public UserShortDto getUserById(Long userId) {
        return mapper.toUserShortDto(repository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с id= %d не найден", userId))));
    }

    @Override
    @Transactional
    public void delete(Long userId) {
        log.info("delete({})", userId);
        User thisUser = repository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        repository.delete(thisUser);
        log.info("Администратором удалён пользователь: {}", thisUser);
    }

    private void validateEmailExist(String email) {
        if (repository.existsByEmail(email)) {
            throw new DuplicatedDataException(String.format("Email - %s уже используется", email));
        }
    }
}
