package ru.practicum.ewm.event.service;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.ewm.contract.StatContract;

@FeignClient(name = "stats-server")
public interface StatClient extends StatContract {
}
