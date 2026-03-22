package ru.practicum.core.eventservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.core.interactionapi.contract.RequestContract;

@FeignClient(name = "request-service")
public interface RequestClient extends RequestContract {
}
