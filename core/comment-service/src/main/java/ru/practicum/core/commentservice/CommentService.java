package ru.practicum.core.commentservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import ru.practicum.core.interactionapi.exception.controller.ErrorHandler;

@Import(ErrorHandler.class)
@EnableDiscoveryClient
@EnableFeignClients
@SpringBootApplication
public class CommentService {

    public static void main(String[] args) {
        SpringApplication.run(CommentService.class, args);
    }
}
