package com.live_price_pipeline.consumer;

import com.live_price_pipeline.model.PriceAlert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Consumes price alert events from the price-alerts topic and broadcasts
 * them to all WebSocket subscribers on /topic/alerts.
 *
 * No persistence here — alerts are ephemeral. A client that connects after
 * an alert fires won't see it, which is intentional for a live feed.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AlertConsumer {

    private final SimpMessagingTemplate messagingTemplate;

    @KafkaListener(
            topics = "${kafka.topics.price-alerts}",
            groupId = "alert-broadcast-group",
            containerFactory = "alertListenerFactory"
    )
    public void consume(ConsumerRecord<String, PriceAlert> record) {
        PriceAlert alert = record.value();
        if (alert == null) return;

        messagingTemplate.convertAndSend("/topic/alerts", alert);
        log.info("[WS] Alert broadcast -> {}", alert);
    }
}