export function StatsBar({ candle, ticker }) {
  if (!candle) return (
    <div className="stats-bar empty">
      <span className="stats-waiting">Waiting for first candle...</span>
    </div>
  )

  const change = candle.open > 0
    ? (((candle.close - candle.open) / candle.open) * 100).toFixed(3)
    : '0.000'
  const positive = parseFloat(change) >= 0

  return (
    <div className="stats-bar">
      <div className="stat-main">
        <span className="stat-ticker">{ticker}</span>
        <span className="stat-price">${Number(candle.close).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</span>
        <span className={`stat-change ${positive ? 'up' : 'down'}`}>
          {positive ? '+' : ''}{change}%
        </span>
      </div>
      <div className="stat-grid">
        <div className="stat-item">
          <span className="stat-label">Open</span>
          <span className="stat-value">${Number(candle.open).toLocaleString('en-US', { minimumFractionDigits: 2 })}</span>
        </div>
        <div className="stat-item">
          <span className="stat-label">High</span>
          <span className="stat-value up">${Number(candle.high).toLocaleString('en-US', { minimumFractionDigits: 2 })}</span>
        </div>
        <div className="stat-item">
          <span className="stat-label">Low</span>
          <span className="stat-value down">${Number(candle.low).toLocaleString('en-US', { minimumFractionDigits: 2 })}</span>
        </div>
        <div className="stat-item">
          <span className="stat-label">Volume</span>
          <span className="stat-value">{Number(candle.volume).toFixed(4)}</span>
        </div>
        <div className="stat-item">
          <span className="stat-label">Ticks</span>
          <span className="stat-value">{candle.tickCount}</span>
        </div>
      </div>
    </div>
  )
}