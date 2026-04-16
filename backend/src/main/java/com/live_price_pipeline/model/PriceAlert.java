package com.live_price_pipeline.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Emitted when a price tick moves beyond the configured threshold percent.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceAlert {

    public enum Direction { UP, DOWN }

    private String ticker;
    private BigDecimal currentPrice;
    private BigDecimal previousPrice;
    private double percentChange;
    private Direction direction;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant timestamp;

    public static PriceAlert of(String ticker, BigDecimal current, BigDecimal previous, double pct) {
        Direction dir = current.compareTo(previous) >= 0 ? Direction.UP : Direction.DOWN;
        return new PriceAlert(ticker, current, previous, Math.abs(pct), dir, Instant.now());
    }

    @Override
    public String toString() {
        return String.format("ALERT [%s] %s %.4f%% | prev=%.2f -> curr=%.2f",
                ticker, direction, percentChange, previousPrice, currentPrice);      // Example: "ALERT [BTC] UP 5.00% | prev=61650.00 -> curr=64732.50"
    }
}