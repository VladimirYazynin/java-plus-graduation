package ru.practicum.core.interactionapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDto {

    private Long id;

    @NotBlank
    @Size(min = 1, max = 50, message = "Длина поля 'name' должен быть в диапазоне от 1 до 50 символов")
    private String name;
}
