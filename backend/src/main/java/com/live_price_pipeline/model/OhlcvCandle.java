package com.live_price_pipeline.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

/**
 * Mutable OHLCV candle built tick-by-tick inside a Kafka Streams tumbling window.
 *
 * Must be a class (not a record) — the aggregator mutates it on every tick:
 *   open   = first tick price in the window
 *   high   = running max
 *   low    = running min
 *   close  = latest tick price (updated every tick)
 *   volume = cumulative sum of all tick volumes
 *
 * windowStart/windowEnd are stamped by the topology when the window closes.
 */
@Data
@NoArgsConstructor
public class OhlcvCandle {

    private String ticker;
    private BigDecimal open;
    private BigDecimal high;
    private BigDecimal low;
    private BigDecimal close;
    private BigDecimal volume = BigDecimal.ZERO;
    private long tickCount = 0;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant windowStart;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant windowEnd;

    public static OhlcvCandle empty() {
        return new OhlcvCandle();
    }

    public OhlcvCandle update(PriceTick tick) {
        if (tickCount == 0) {
            this.ticker = tick.ticker();
            this.open   = tick.price();
            this.high   = tick.price();
            this.low    = tick.price();
        } else {
            if (tick.price().compareTo(this.high) > 0) this.high = tick.price();
            if (tick.price().compareTo(this.low)  < 0) this.low  = tick.price();
        }
        this.close  = tick.price();
        this.volume = this.volume.add(tick.volume()).setScale(4, RoundingMode.HALF_UP);
        this.tickCount++;
        return this;
    }

    @Override
    public String toString() {
        return String.format("[%s] O=%.2f H=%.2f L=%.2f C=%.2f V=%.4f ticks=%d",
                ticker, open, high, low, close, volume, tickCount);
    }
}