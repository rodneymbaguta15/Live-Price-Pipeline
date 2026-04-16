package com.live_price_pipeline.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Immutable value object representing a single price tick event.
 * Serialized as JSON when published to Kafka.
 *
 * ticker    — asset symbol, e.g. "BTC". Also used as the Kafka message key
 *             so all ticks for the same asset go to the same partition.
 * price     — current mid price in USD
 * volume    — simulated trade volume for this tick
 * timestamp — event time (used by Kafka Streams windowing)
 */

public record PriceTick(
        String ticker,
        BigDecimal price,
        BigDecimal volume,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Instant timestamp
) {
    public static PriceTick of(String ticker, BigDecimal price, BigDecimal volume) {
        return new PriceTick(ticker, price, volume, Instant.now());
    }
}
