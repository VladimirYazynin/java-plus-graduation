package ru.practicum.core.commentservice.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.core.interactionapi.dto.CommentDto;
import ru.practicum.core.interactionapi.dto.FullCommentDto;
import ru.practicum.core.commentservice.model.Comment;

@Component
public class CommentMapper {

    public FullCommentDto toFullCommentDto(Comment comment) {
        FullCommentDto commentDto = new FullCommentDto();
        commentDto.setId(comment.getId());
        commentDto.setEventId(comment.getEventId());
        commentDto.setAuthorName(comment.getAuthor().getName());
        commentDto.setText(comment.getText());
        commentDto.setCreated(comment.getCreated());
        return commentDto;
    }

    public CommentDto toCommentDto(Comment comment) {
        return new CommentDto(comment.getEventId(), comment.getText());
    }
}