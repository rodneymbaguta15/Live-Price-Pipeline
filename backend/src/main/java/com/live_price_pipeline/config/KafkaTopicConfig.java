package com.live_price_pipeline.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.config.TopicBuilder;

/** Centralized Kafka topic configuration.
 * Defines the topics used by the application and their properties.
 */
@Configuration
@EnableKafkaStreams
public class KafkaTopicConfig {

    @Value("${kafka.topics.raw-ticks}")
    private String rawTicksTopic;

    @Value("${kafka.topics.ohlcv-candles}")
    private String ohlcvCandlesTopic;

    @Value("${kafka.topics.price-alerts}")
    private String priceAlertsTopic;

    @Bean
    public NewTopic rawPriceTicksTopic() {
        return TopicBuilder.name(rawTicksTopic).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic ohlcvCandlesTopic() {
        return TopicBuilder.name(ohlcvCandlesTopic).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic priceAlertsTopic() {
        return TopicBuilder.name(priceAlertsTopic).partitions(1).replicas(1).build();
    }
}