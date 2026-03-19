package ru.practicum.core.eventservice.category.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.core.interactionapi.dto.CategoryDto;
import ru.practicum.core.eventservice.category.service.CategoryService;

import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestController
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService service;

    @PostMapping("/admin/categories")
    @ResponseStatus(CREATED)
    public CategoryDto create(@Valid @RequestBody CategoryDto category) {
        return service.create(category);
    }

    @PatchMapping("/admin/categories/{catId}")
    public CategoryDto update(@PathVariable Integer catId,
                              @Valid @RequestBody CategoryDto category) {
        return service.update(catId, category);
    }

    @DeleteMapping("/admin/categories/{catId}")
    @ResponseStatus(NO_CONTENT)
    public void delete(@PathVariable Integer catId) {
        service.delete(catId);
    }

    @GetMapping("/categories")
    public List<CategoryDto> get(@RequestParam(defaultValue = "0") int from,
                                 @RequestParam(defaultValue = "10") int size) {
        return service.get(from, size);
    }

    @GetMapping("/categories/{catId}")
    public CategoryDto getById(@PathVariable Integer catId) {
        return service.getById(catId);
    }
}
