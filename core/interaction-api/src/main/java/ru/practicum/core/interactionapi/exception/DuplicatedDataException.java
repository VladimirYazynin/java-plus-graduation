package ru.practicum.core.interactionapi.exception;

public class DuplicatedDataException extends RuntimeException {

    public DuplicatedDataException(String message) {
        super(message);
    }
}
