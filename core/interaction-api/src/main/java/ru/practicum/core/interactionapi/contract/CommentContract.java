package ru.practicum.core.interactionapi.contract;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.practicum.core.interactionapi.dto.CommentDto;
import ru.practicum.core.interactionapi.dto.CommentShort;
import ru.practicum.core.interactionapi.dto.FullCommentDto;

import java.util.List;

public interface CommentContract {

    @PostMapping("/users/{userId}/comments")
    FullCommentDto create(@PathVariable Long userId, @Valid @RequestBody CommentDto newCommentDto);

    @PatchMapping("/users/{userId}/comments/{commentId}")
    FullCommentDto update(@PathVariable Long userId,
                          @PathVariable Long commentId,
                          @Valid @RequestBody CommentDto updatedCommentDto);

    @DeleteMapping("/users/{userId}/comments/{commentId}")
    void deleteOwnComment(@PathVariable Long userId, @PathVariable Long commentId);

    @DeleteMapping("/admin/comments/{commentId}")
    void deleteComment(@PathVariable Long commentId);

    @GetMapping("/admin/comments/{eventId}")
    List<CommentShort> getCommentsForEvent(@PathVariable Long eventId);
}
