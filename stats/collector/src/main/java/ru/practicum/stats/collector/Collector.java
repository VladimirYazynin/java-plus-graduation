package ru.practicum.stats.collector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class Collector {

    public static void main(String[] args) {
        SpringApplication.run(Collector.class, args);
    }
}
