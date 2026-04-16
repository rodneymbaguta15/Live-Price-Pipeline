package com.live_price_pipeline.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * JPA entity for persisting completed OHLCV candles to H2 / PostgreSQL.
 *
 * Intentionally separate from OhlcvCandle (the Kafka Streams model):
 *   OhlcvCandle       -> mutable, lives inside the Streams aggregator
 *   OhlcvCandleEntity -> persisted record, never mutated after save
 *
 * The CandleConsumer maps from OhlcvCandle to this entity using
 * an upsert pattern: update the row if it already exists for the same
 * ticker + windowStart, otherwise insert. This prevents duplicate rows
 * from the intermediate window updates emitted by Kafka Streams.
 */
@Data
@NoArgsConstructor
@Entity
@Table(
        name = "ohlcv_candle",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_ticker_window", columnNames = {"ticker", "windowStart"})
        },
        indexes = {
                @Index(name = "idx_ticker_window_start", columnList = "ticker, windowStart DESC")
        }
)
public class OhlcvCandleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String ticker;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal open;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal high;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal low;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal close;

    @Column(nullable = false, precision = 18, scale = 4)
    private BigDecimal volume;

    @Column(nullable = false)
    private long tickCount;

    @Column(nullable = false)
    private Instant windowStart;

    @Column(nullable = false)
    private Instant windowEnd;

    public OhlcvCandleEntity(String ticker, BigDecimal open, BigDecimal high,
                             BigDecimal low, BigDecimal close, BigDecimal volume,
                             long tickCount, Instant windowStart, Instant windowEnd) {
        this.ticker      = ticker;
        this.open        = open;
        this.high        = high;
        this.low         = low;
        this.close       = close;
        this.volume      = volume;
        this.tickCount   = tickCount;
        this.windowStart = windowStart;
        this.windowEnd   = windowEnd;
    }
}