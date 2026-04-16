package com.live_price_pipeline.consumer;

import com.live_price_pipeline.entity.OhlcvCandleEntity;
import com.live_price_pipeline.model.OhlcvCandle;
import com.live_price_pipeline.repository.OhlcvCandleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Consumes completed OHLCV candles from the ohlcv-candles topic.
 *
 * Two responsibilities per message:
 *   1. Persist to H2/Postgres via upsert (update if same ticker+windowStart
 *      already exists, otherwise insert). Kafka Streams emits intermediate
 *      window updates on every tick, so we'd get many rows per window without
 *      this logic.
 *   2. Broadcast the candle to all WebSocket subscribers on
 *      /topic/prices/{ticker} so the frontend chart updates in real time.
 *
 * SimpMessagingTemplate.convertAndSend() serializes the OhlcvCandle to JSON
 * and pushes it to every client subscribed to that destination. Zero
 * per-client logic needed here — the broker handles fan-out.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CandleConsumer {

    private final OhlcvCandleRepository repository;
    private final SimpMessagingTemplate messagingTemplate;

    @KafkaListener(
            topics = "${kafka.topics.ohlcv-candles}",
            groupId = "candle-persistence-group",
            containerFactory = "candleListenerFactory"
    )
    public void consume(ConsumerRecord<String, OhlcvCandle> record) {
        OhlcvCandle candle = record.value();
        if (candle == null || candle.getTicker() == null || candle.getWindowStart() == null) return;

        // 1. Persist — upsert by ticker + windowStart
        upsert(candle);

        // 2. Broadcast to WebSocket subscribers
        String destination = "/topic/prices/" + candle.getTicker();
        messagingTemplate.convertAndSend(destination, candle);

        log.debug("[WS] Pushed candle to {} -> {}", destination, candle);
    }

    private void upsert(OhlcvCandle candle) {
        repository.findByTickerAndWindowStart(candle.getTicker(), candle.getWindowStart())
                .ifPresentOrElse(
                        existing -> {
                            // Update the existing row with latest OHLCV values from this window
                            existing.setHigh(candle.getHigh());
                            existing.setLow(candle.getLow());
                            existing.setClose(candle.getClose());
                            existing.setVolume(candle.getVolume());
                            existing.setTickCount(candle.getTickCount());
                            repository.save(existing);
                            log.debug("[DB] Updated candle [{} @ {}] ticks={}",
                                    candle.getTicker(), candle.getWindowStart(), candle.getTickCount());
                        },
                        () -> {
                            // New window — insert a fresh row
                            OhlcvCandleEntity entity = new OhlcvCandleEntity(
                                    candle.getTicker(),
                                    candle.getOpen(),
                                    candle.getHigh(),
                                    candle.getLow(),
                                    candle.getClose(),
                                    candle.getVolume(),
                                    candle.getTickCount(),
                                    candle.getWindowStart(),
                                    candle.getWindowEnd()
                            );
                            repository.save(entity);
                            log.debug("[DB] Inserted candle [{} @ {}]",
                                    candle.getTicker(), candle.getWindowStart());
                        }
                );
    }
}