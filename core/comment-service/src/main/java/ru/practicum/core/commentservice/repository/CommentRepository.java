package ru.practicum.core.commentservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.core.interactionapi.dto.CommentShort;
import ru.practicum.core.commentservice.model.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    boolean existsByIdAndAuthor(Long id, Long author);

    List<CommentShort> getCommentsByEventId(Long eventId);
}