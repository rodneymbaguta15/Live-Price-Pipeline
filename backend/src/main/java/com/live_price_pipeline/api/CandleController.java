package com.live_price_pipeline.api;

import com.live_price_pipeline.entity.OhlcvCandleEntity;
import com.live_price_pipeline.repository.OhlcvCandleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST endpoint serving OHLCV candle history to the frontend.
 *
 * The React dashboard calls this on mount to pre-populate the chart
 * before the WebSocket stream starts. Without this, the chart would
 * start empty and only show candles that arrive after page load.
 *
 * GET /api/candles/{ticker}?limit=100
 *   Returns the most recent N candles for the given ticker, newest first.
 *   The frontend reverses the list to display oldest-to-newest on the chart.
 *
 * @CrossOrigin allows requests from the Vite dev server on port 5173. Restrict this in production.
 */
@Slf4j
@RestController
@RequestMapping("/api/candles")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
@RequiredArgsConstructor
public class CandleController {

    private final OhlcvCandleRepository repository;

    @GetMapping("/{ticker}")
    public ResponseEntity<List<OhlcvCandleEntity>> getCandles(
            @PathVariable String ticker,
            @RequestParam(defaultValue = "100") int limit) {

        String upperTicker = ticker.toUpperCase();
        List<OhlcvCandleEntity> candles = repository
                .findByTickerOrderByWindowStartDesc(upperTicker, PageRequest.of(0, limit));

        log.debug("GET /api/candles/{} limit={} -> {} rows", upperTicker, limit, candles.size());
        return ResponseEntity.ok(candles);
    }

    @GetMapping("/tickers")
    public ResponseEntity<List<String>> getAvailableTickers() {
        // Convenience endpoint so the frontend can discover which tickers have data
        List<String> tickers = repository.findAll()
                .stream()
                .map(OhlcvCandleEntity::getTicker)
                .distinct()
                .sorted()
                .toList();
        return ResponseEntity.ok(tickers);
    }
}