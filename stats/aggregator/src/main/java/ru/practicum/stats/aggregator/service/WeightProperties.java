package ru.practicum.stats.aggregator.service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;

import java.util.Map;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "coefficients")
public class WeightProperties {

    Map<ActionTypeAvro, Double> weights;
}
