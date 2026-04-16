package com.live_price_pipeline.repository;

import com.live_price_pipeline.entity.OhlcvCandleEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface OhlcvCandleRepository extends JpaRepository<OhlcvCandleEntity, Long> {

    /**
     * Find the most recent N candles for a ticker, newest first.
     * Used by the REST endpoint to serve candle history to the frontend on load.
     */
    List<OhlcvCandleEntity> findByTickerOrderByWindowStartDesc(String ticker, Pageable pageable);

    /**
     * Find an existing candle by ticker + window start.
     * Used by CandleConsumer to upsert: update if exists, insert if not.
     * This prevents duplicate rows from intermediate window updates.
     */
    Optional<OhlcvCandleEntity> findByTickerAndWindowStart(String ticker, Instant windowStart);
}