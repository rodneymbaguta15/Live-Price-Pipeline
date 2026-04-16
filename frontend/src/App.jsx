import { useState, useEffect } from 'react'
import { useWebSocket } from './hooks/useWebSocket'
import { fetchCandles } from './services/candleApi'
import { TickerSelector } from './components/TickerSelector'
import { StatsBar } from './components/StatsBar'
import { PriceChart } from './components/PriceChart'
import { AlertFeed } from './components/AlertFeed'

const TICKERS = ['BTC', 'ETH', 'SOL']

export default function App() {
  const [selectedTicker, setSelectedTicker] = useState('BTC')
  const { candlesByTicker, alerts, connected } = useWebSocket()

  // Load candle history from REST API on mount for all tickers
  useEffect(() => {
    TICKERS.forEach(async ticker => {
      try {
        const history = await fetchCandles(ticker, 60)
        if (history.length > 0) {
          // Dispatch a synthetic event to seed the WebSocket hook's state
          // by triggering a state update through the hook's upsert logic.
          // We do this by importing a shared setter — instead, App manages
          // historical candles separately and merges with live.
        }
      } catch (e) {
        console.warn(`Could not load history for ${ticker}:`, e.message)
      }
    })
  }, [])

  const [historicalCandles, setHistoricalCandles] = useState({ BTC: [], ETH: [], SOL: [] })

  useEffect(() => {
    TICKERS.forEach(async ticker => {
      try {
        const history = await fetchCandles(ticker, 60)
        setHistoricalCandles(prev => ({ ...prev, [ticker]: history }))
      } catch (e) {
        console.warn(`History fetch failed for ${ticker}:`, e.message)
      }
    })
  }, [])

  // Merge historical + live: live candles upsert on top of historical
  function mergedCandles(ticker) {
    const hist = historicalCandles[ticker] ?? []
    const live = candlesByTicker[ticker] ?? []
    if (live.length === 0) return hist

    const map = new Map()
    hist.forEach(c => map.set(c.windowStart, c))
    live.forEach(c => map.set(c.windowStart, c))

    return Array.from(map.values())
      .sort((a, b) => new Date(a.windowStart) - new Date(b.windowStart))
      .slice(-60)
  }

  const candles = mergedCandles(selectedTicker)
  const latestCandle = candles.length > 0 ? candles[candles.length - 1] : null

  return (
    <div className="app">
      <header className="app-header">
        <div className="header-left">
          <div className="logo">
            <span className="logo-icon">◈</span>
            <span className="logo-text">LivePrice</span>
            <span className="logo-sub">STREAM</span>
          </div>
        </div>
        <div className="header-center">
          <TickerSelector selected={selectedTicker} onChange={setSelectedTicker} />
        </div>
        <div className="header-right">
          <div className={`ws-badge ${connected ? 'live' : 'offline'}`}>
            <span className="ws-dot" />
            {connected ? 'LIVE' : 'CONNECTING'}
          </div>
        </div>
      </header>

      <main className="app-main">
        <div className="main-left">
          <StatsBar candle={latestCandle} ticker={selectedTicker} />
          <div className="chart-section">
            <div className="section-label">
              {selectedTicker} / USD &nbsp;·&nbsp; 1m candles
            </div>
            <PriceChart candles={candles} ticker={selectedTicker} />
          </div>
        </div>
        <aside className="main-right">
          <AlertFeed alerts={alerts} />
        </aside>
      </main>
    </div>
  )
}