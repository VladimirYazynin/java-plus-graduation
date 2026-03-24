package ru.practicum.core.eventservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import ru.practicum.core.interactionapi.exception.controller.ErrorHandler;
import ru.practicum.stats.statsclient.AnalyzerClient;
import ru.practicum.stats.statsclient.CollectorClient;

@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication
@Import({AnalyzerClient.class, CollectorClient.class, ErrorHandler.class})
public class EventService {

    public static void main(String[] args) {
        SpringApplication.run(EventService.class, args);
    }
}
