package com.live_price_pipeline.producer;

import com.live_price_pipeline.model.PriceTick;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Generates simulated price ticks using a random walk.
 *
 * Each call to fetchTick() moves the price +/-0.5% from its last value,
 * producing realistic-looking price movement without hitting an external API.
 *
 * ConcurrentHashMap is used for lastPrices because the @Scheduled task runs
 * on a thread pool — if you ever increase pool size beyond 1, state is safe.
 */
@Slf4j
@Service
public class PriceDataService {

    private static final double VOLATILITY = 0.005;

    private final Random rng = new Random();

    private final Map<String, BigDecimal> lastPrices = new ConcurrentHashMap<>(Map.of(
            "BTC", new BigDecimal("65000.00"),
            "ETH", new BigDecimal("3200.00"),
            "SOL", new BigDecimal("145.00")
    ));

    public PriceTick fetchTick(String ticker) {
        BigDecimal last = lastPrices.get(ticker);
        if (last == null) {
            throw new IllegalArgumentException("Unknown ticker: " + ticker);
        }

        double move = 1.0 + (rng.nextDouble() - 0.5) * 2 * VOLATILITY;
        BigDecimal newPrice = last
                .multiply(BigDecimal.valueOf(move))
                .setScale(2, RoundingMode.HALF_UP);

        lastPrices.put(ticker, newPrice);

        BigDecimal volume = BigDecimal.valueOf(0.01 + rng.nextDouble() * 4.99)
                .setScale(4, RoundingMode.HALF_UP);

        log.debug("[{}] price={} volume={}", ticker, newPrice, volume);

        return PriceTick.of(ticker, newPrice, volume);
    }
}
