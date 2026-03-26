package ru.practicum.stats.analyzer.service;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
@ConfigurationProperties(prefix = "analyzer.weights")
public class WeightProperties {

    private Double view;
    private Double register;
    private Double like;
}
