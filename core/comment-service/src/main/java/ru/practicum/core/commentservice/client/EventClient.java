package ru.practicum.core.commentservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.core.interactionapi.contract.EventContract;

@FeignClient(name = "event-service")
public interface EventClient extends EventContract {
}
