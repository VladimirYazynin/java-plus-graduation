package ru.practicum.core.eventservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.core.interactionapi.contract.CommentContract;

@FeignClient(name = "comment-service")
public interface CommentClient extends CommentContract {
}
