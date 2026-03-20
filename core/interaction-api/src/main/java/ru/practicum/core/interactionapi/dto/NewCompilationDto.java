package ru.practicum.core.interactionapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewCompilationDto {

    private Long id;
    private List<Long> events;
    private Boolean pinned;

    @NotBlank
    @Size(min = 1, max = 50, message = "Длина поля 'title' должен быть в диапазоне от 1 до 50 символов")
    private String title;
}
