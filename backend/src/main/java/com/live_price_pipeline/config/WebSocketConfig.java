package com.live_price_pipeline.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configures Spring's STOMP-over-WebSocket message broker.
 *
 * Clients connect to ws://localhost:8080/ws using SockJS.
 * They subscribe to named channels: /topic/prices/{ticker} or /topic/alerts.
 * The backend pushes to those channels via SimpMessagingTemplate.
 *
 * Why STOMP over raw WebSocket?
 * STOMP gives named destinations so multiple clients can subscribe to different
 * tickers independently. The broker handles fan-out — one convertAndSend()
 * call reaches every connected subscriber on that channel automatically.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("http://localhost:5173", "http://localhost:3000")
                .withSockJS();
    }
}