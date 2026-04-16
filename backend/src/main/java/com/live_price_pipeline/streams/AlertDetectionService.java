package com.live_price_pipeline.streams;

import com.live_price_pipeline.model.PriceAlert;
import com.live_price_pipeline.model.PriceTick;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks last seen price per ticker and evaluates whether a new tick
 * constitutes a price alert.
 *
 * Threshold is 0.3% so alerts fire visibly with simulated +/-0.5% data.
 * Raise to 1-2% for production use.
 */
@Slf4j
@Service
public class AlertDetectionService {

    @Value("${alerts.threshold-percent}")
    private double thresholdPercent;

    private final Map<String, BigDecimal> lastPrices = new ConcurrentHashMap<>();

    public Optional<PriceAlert> evaluate(PriceTick tick) {
        BigDecimal previous = lastPrices.put(tick.ticker(), tick.price());
        if (previous == null) return Optional.empty();

        double pct = percentChange(previous, tick.price());
        if (Math.abs(pct) >= thresholdPercent) {
            log.debug("Alert triggered for {}: {}%", tick.ticker(), String.format("%.4f", pct));
            return Optional.of(PriceAlert.of(tick.ticker(), tick.price(), previous, pct));
        }
        return Optional.empty();
    }

    private double percentChange(BigDecimal from, BigDecimal to) {
        if (from.compareTo(BigDecimal.ZERO) == 0) return 0;
        return to.subtract(from)
                .divide(from, 8, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }
}