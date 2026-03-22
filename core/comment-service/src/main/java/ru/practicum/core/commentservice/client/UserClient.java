package ru.practicum.core.commentservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.core.interactionapi.contract.UserContract;

@FeignClient(name = "user-service", path = "/admin/users")
public interface UserClient extends UserContract {
}
