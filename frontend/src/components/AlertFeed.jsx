function timeAgo(isoString) {
  if (!isoString) return ''
  const diff = Math.floor((Date.now() - new Date(isoString).getTime()) / 1000)
  if (diff < 60) return `${diff}s ago`
  if (diff < 3600) return `${Math.floor(diff / 60)}m ago`
  return `${Math.floor(diff / 3600)}h ago`
}

const TICKER_COLORS = { BTC: '#F7931A', ETH: '#627EEA', SOL: '#9945FF' }

export function AlertFeed({ alerts }) {
  return (
    <div className="alert-feed">
      <div className="alert-feed-header">
        <span className="alert-feed-title">Price Alerts</span>
        <span className="alert-count">{alerts.length}</span>
      </div>
      <div className="alert-list">
        {alerts.length === 0 ? (
          <div className="alert-empty">Monitoring for moves &gt; 0.3%...</div>
        ) : (
          alerts.map(a => (
            <div key={a.id} className={`alert-item ${a.direction === 'UP' ? 'alert-up' : 'alert-down'}`}>
              <div className="alert-left">
                <span className="alert-ticker" style={{ color: TICKER_COLORS[a.ticker] }}>
                  {a.ticker}
                </span>
                <span className={`alert-dir ${a.direction === 'UP' ? 'up' : 'down'}`}>
                  {a.direction === 'UP' ? '▲' : '▼'}
                </span>
                <span className={`alert-pct ${a.direction === 'UP' ? 'up' : 'down'}`}>
                  {a.percentChange.toFixed(3)}%
                </span>
              </div>
              <div className="alert-right">
                <span className="alert-price">${Number(a.currentPrice).toLocaleString('en-US', { minimumFractionDigits: 2 })}</span>
                <span className="alert-time">{timeAgo(a.timestamp)}</span>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  )
}