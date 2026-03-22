package ru.practicum.core.commentservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.core.commentservice.client.EventClient;
import ru.practicum.core.commentservice.client.UserClient;
import ru.practicum.core.commentservice.mapper.CommentMapper;
import ru.practicum.core.commentservice.model.Comment;
import ru.practicum.core.commentservice.repository.CommentRepository;
import ru.practicum.core.interactionapi.dto.*;
import ru.practicum.core.interactionapi.exception.AccessException;
import ru.practicum.core.interactionapi.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserClient userClient;
    private final EventClient eventClient;
    private final CommentMapper mapper;

    @Override
    @Transactional
    public FullCommentDto addCommentToEventByUser(Long authorId, CommentDto newCommentDto) {
        UserShortDto user = userClient.getUserById(authorId);
        eventClient.getEventById(newCommentDto.getEventId());
        Comment newComment = new Comment();
        newComment.setEventId(newCommentDto.getEventId());
        newComment.setAuthor(user.getId());
        newComment.setText(newCommentDto.getText());
        newComment.setCreated(LocalDateTime.now());
        FullCommentDto response = mapper.toFullCommentDto(commentRepository.save(newComment));
        response.setAuthorName(user.getName());
        return response;
    }

    @Override
    @Transactional
    public FullCommentDto updateCommentByUser(Long authorId, Long commentId, CommentDto updatedCommentDto) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий не найден"));
        if (!comment.getAuthor().equals(authorId)) {
            throw new AccessException("Не доступно для редактирования");
        }
        UserShortDto author = userClient.getUserById(authorId);
        comment.setEventId(updatedCommentDto.getEventId());
        comment.setText(updatedCommentDto.getText());
        FullCommentDto response = mapper.toFullCommentDto(commentRepository.save(comment));
        response.setAuthorName(author.getName());
        return response;
    }

    @Override
    @Transactional
    public void deleteOwnComment(Long userId, Long commentId) {
        if (commentRepository.existsByIdAndAuthor(commentId, userId)) {
            commentRepository.deleteById(commentId);
        } else {
            throw new NotFoundException("Комментарий не найден");
        }
    }

    @Override
    @Transactional
    public void deleteCommentById(Long commentId) {
        if (commentRepository.existsById(commentId)) {
            commentRepository.deleteById(commentId);
        } else {
            throw new NotFoundException("Комментарий не найден");
        }
    }

    public List<CommentShort> getCommentsForEvent(Long eventId) {
        return commentRepository.getCommentsByEventId(eventId);
    }
}