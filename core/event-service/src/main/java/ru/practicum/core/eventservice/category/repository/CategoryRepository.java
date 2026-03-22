package ru.practicum.core.eventservice.category.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.core.eventservice.category.model.Category;

import java.util.Collection;
import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    boolean existsByName(String name);

    Category findByName(String name);

    List<Category> findAllByIdIn(Collection<Integer> id);
}
