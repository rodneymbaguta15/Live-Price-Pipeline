package com.live_price_pipeline.streams;

import com.live_price_pipeline.model.OhlcvCandle;
import com.live_price_pipeline.model.PriceAlert;
import com.live_price_pipeline.model.PriceTick;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JsonSerde;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Kafka Streams topology for processing raw price ticks into OHLCV candles and price alerts.
 *
 * Reads from raw-price-ticks, forks into two in-memory branches:
 *
 *   Branch 1 — OHLCV aggregation
 *     groupByKey → 1-min tumbling window → aggregate(OhlcvCandle)
 *     → selectKey (unwrap Windowed key) → ohlcv-candles topic
 *
 *   Branch 2 — Alert detection
 *     flatMapValues via AlertDetectionService → price-alerts topic
 *
 * Both branches share the same source KStream with no double Kafka read.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class PriceStreamTopology {

    private final AlertDetectionService alertDetectionService;

    @Value("${kafka.topics.raw-ticks}")
    private String rawTicksTopic;

    @Value("${kafka.topics.ohlcv-candles}")
    private String ohlcvCandlesTopic;

    @Value("${kafka.topics.price-alerts}")
    private String priceAlertsTopic;

    @Bean
    public KStream<String, PriceTick> priceTickStream(StreamsBuilder streamsBuilder) {

        JsonSerde<PriceTick>   tickSerde   = new JsonSerde<>(PriceTick.class);
        JsonSerde<OhlcvCandle> candleSerde = new JsonSerde<>(OhlcvCandle.class);
        JsonSerde<PriceAlert>  alertSerde  = new JsonSerde<>(PriceAlert.class);

        // Source stream
        KStream<String, PriceTick> rawStream = streamsBuilder.stream(
                rawTicksTopic,
                Consumed.with(Serdes.String(), tickSerde)
        );

        // Branch 1: 1-minute OHLCV tumbling window
        rawStream
                .groupByKey(Grouped.with(Serdes.String(), tickSerde))
                .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofMinutes(1)))
                .aggregate(
                        OhlcvCandle::empty,
                        (ticker, tick, candle) -> candle.update(tick),
                        Materialized.with(Serdes.String(), candleSerde)
                )
                .toStream()
                .peek((windowedKey, candle) -> {
                    if (candle != null && candle.getTicker() != null) {
                        candle.setWindowStart(Instant.ofEpochMilli(windowedKey.window().start()));
                        candle.setWindowEnd(Instant.ofEpochMilli(windowedKey.window().end()));
                        log.debug("Window tick -> {}", candle);
                    }
                })
                .selectKey((windowedKey, candle) -> windowedKey.key())
                .to(ohlcvCandlesTopic, Produced.with(Serdes.String(), candleSerde));

        // Branch 2: Alert detection
        rawStream
                .flatMapValues(tick ->
                        alertDetectionService.evaluate(tick)
                                .map(List::of)
                                .orElse(List.of())
                )
                .peek((key, alert) -> log.debug("Alert emitted -> {}", alert))
                .to(priceAlertsTopic, Produced.with(Serdes.String(), alertSerde));

        return rawStream;
    }
}