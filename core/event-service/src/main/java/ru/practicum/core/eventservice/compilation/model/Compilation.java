package ru.practicum.core.eventservice.compilation.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.*;
import ru.practicum.core.eventservice.event.model.Event;

import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.GenerationType.IDENTITY;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "compilations")
public class Compilation {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;
    @ManyToMany
    private List<Event> events = new ArrayList<>();
    private Boolean pinned;
    private String title;
}
