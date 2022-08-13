package ru.currency_pair.statistics.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "application")
@Data
@NoArgsConstructor
public class StatisticsConfig {

    List<String> currencyPairs;

    List<Integer> timeframe;

}
