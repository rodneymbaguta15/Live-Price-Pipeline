const TICKERS = ['BTC', 'ETH', 'SOL']

const COLORS = { BTC: '#F7931A', ETH: '#627EEA', SOL: '#9945FF' }

export function TickerSelector({ selected, onChange }) {
  return (
    <div className="ticker-selector">
      {TICKERS.map(t => (
        <button
          key={t}
          className={`ticker-btn ${selected === t ? 'active' : ''}`}
          style={{ '--accent': COLORS[t] }}
          onClick={() => onChange(t)}
        >
          {t}
        </button>
      ))}
    </div>
  )
}