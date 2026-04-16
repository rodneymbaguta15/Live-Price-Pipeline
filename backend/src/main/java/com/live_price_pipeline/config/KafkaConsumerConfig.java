package com.live_price_pipeline.config;

import com.live_price_pipeline.model.OhlcvCandle;
import com.live_price_pipeline.model.PriceAlert;
import com.live_price_pipeline.model.PriceTick;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

/**
*Centralized Kafka consumer configuration.
*Defines multiple container factories for different message types and consumer groups.
*/

@Configuration
public class KafkaConsumerConfig {

    private Map<String, Object> baseProps() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName());
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        return props;
    }

    private <T> ConcurrentKafkaListenerContainerFactory<String, T> factory(
            Class<T> targetType, String groupId) {
        Map<String, Object> props = baseProps();
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, targetType.getName());
        ConsumerFactory<String, T> cf = new DefaultKafkaConsumerFactory<>(props);
        ConcurrentKafkaListenerContainerFactory<String, T> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(cf);
        return factory;
    }

    // Default factory — used by consumeRawTick (no containerFactory attribute)
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PriceTick>
    kafkaListenerContainerFactory() {
        return factory(PriceTick.class, "price-consumer-group");
    }

    @Bean("candleListenerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, OhlcvCandle>
    candleListenerFactory() {
        return factory(OhlcvCandle.class, "candle-verification-group");
    }

    @Bean("alertListenerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, PriceAlert>
    alertListenerFactory() {
        return factory(PriceAlert.class, "alert-verification-group");
    }
}