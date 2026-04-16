import { useEffect, useRef, useState, useCallback } from 'react'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

const WS_URL = 'http://localhost:8080/ws'
const TICKERS = ['BTC', 'ETH', 'SOL']
const MAX_CANDLES = 60

/**
 * Custom hook managing the STOMP WebSocket lifecycle.
 *
 * Connects on mount, subscribes to /topic/prices/{ticker} for all three
 * tickers and /topic/alerts. Cleans up the client on unmount.
 *
 * Candles are upserted by windowStart so the current in-progress window
 * updates in-place rather than appending duplicates on every tick.
 *
 * Returns:
 *   candlesByTicker  { BTC: [...], ETH: [...], SOL: [...] } oldest-first
 *   alerts           newest-first, capped at 50
 *   connected        boolean WebSocket status
 */
export function useWebSocket() {
  const clientRef = useRef(null)
  const [connected, setConnected] = useState(false)
  const [alerts, setAlerts] = useState([])
  const [candlesByTicker, setCandlesByTicker] = useState({
    BTC: [], ETH: [], SOL: []
  })

  const upsertCandle = useCallback((ticker, incoming) => {
    setCandlesByTicker(prev => {
      const existing = prev[ticker] ?? []
      const idx = existing.findIndex(c => c.windowStart === incoming.windowStart)
      let updated
      if (idx >= 0) {
        updated = [...existing]
        updated[idx] = incoming
      } else {
        updated = [...existing, incoming]
      }
      if (updated.length > MAX_CANDLES) updated = updated.slice(-MAX_CANDLES)
      return { ...prev, [ticker]: updated }
    })
  }, [])

  useEffect(() => {
    const client = new Client({
      webSocketFactory: () => new SockJS(WS_URL),
      reconnectDelay: 3000,
      onConnect: () => {
        setConnected(true)
        TICKERS.forEach(ticker => {
          client.subscribe(`/topic/prices/${ticker}`, msg => {
            try {
              const candle = JSON.parse(msg.body)
              if (candle?.ticker) upsertCandle(candle.ticker, candle)
            } catch (e) { console.error('Candle parse error', e) }
          })
        })
        client.subscribe('/topic/alerts', msg => {
          try {
            const alert = JSON.parse(msg.body)
            if (alert?.ticker) {
              setAlerts(prev => [
                { ...alert, id: Date.now() + Math.random() },
                ...prev.slice(0, 49)
              ])
            }
          } catch (e) { console.error('Alert parse error', e) }
        })
      },
      onDisconnect: () => setConnected(false),
      onStompError: frame => { console.error('STOMP error', frame); setConnected(false) }
    })
    client.activate()
    clientRef.current = client
    return () => { client.deactivate() }
  }, [upsertCandle])

  return { candlesByTicker, alerts, connected }
}