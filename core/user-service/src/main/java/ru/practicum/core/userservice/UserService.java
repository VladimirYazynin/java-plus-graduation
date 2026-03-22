package ru.practicum.core.userservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Import;
import ru.practicum.core.interactionapi.exception.controller.ErrorHandler;

@Import(ErrorHandler.class)
@EnableDiscoveryClient
@SpringBootApplication
public class UserService {

    public static void main(String[] args) {
        SpringApplication.run(UserService.class, args);
    }
}
