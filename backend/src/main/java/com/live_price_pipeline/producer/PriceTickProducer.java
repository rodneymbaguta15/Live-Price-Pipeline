package com.live_price_pipeline.producer;

import com.live_price_pipeline.model.PriceTick;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Scheduled Kafka producer that emits price ticks for BTC, ETH, and SOL.
 *
 * Kafka message key = ticker symbol. This is critical: Kafka routes messages
 * with the same key to the same partition, so all BTC ticks stay ordered on
 * partition 0, all ETH on partition 1, all SOL on partition 2. The Streams
 * topology in Phase 2 depends on this for correct OHLCV aggregation.
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class PriceTickProducer {

    private final KafkaTemplate<String, PriceTick> kafkaTemplate;
    private final PriceDataService priceDataService;

    @Value("${kafka.topics.raw-ticks}")
    private String rawTicksTopic;

    @Value("#{'${producer.tickers}'.split(',')}")
    private List<String> tickers;

    @Scheduled(fixedRateString = "${producer.interval-ms}")
    public void emitTicks() {
        tickers.forEach(ticker -> {
            try {
                PriceTick tick = priceDataService.fetchTick(ticker);

                kafkaTemplate.send(rawTicksTopic, ticker, tick)
                        .whenComplete((result, ex) -> {
                            if (ex != null) {
                                log.error("Failed to publish tick for {}: {}", ticker, ex.getMessage());
                            } else {
                                log.debug("Published [{}] → partition={} offset={}",
                                        ticker,
                                        result.getRecordMetadata().partition(),
                                        result.getRecordMetadata().offset());
                            }
                        });
            } catch (Exception e) {
                log.error("Error generating tick for {}: {}", ticker, e.getMessage());
            }
        });
    }
}
