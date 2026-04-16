import {
  ResponsiveContainer, LineChart, Line,
  XAxis, YAxis, Tooltip, CartesianGrid, ReferenceLine
} from 'recharts'

const TICKER_COLORS = { BTC: '#F7931A', ETH: '#627EEA', SOL: '#9945FF' }

function formatTime(isoString) {
  if (!isoString) return ''
  const d = new Date(isoString)
  return d.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit', second: '2-digit', hour12: false })
}

function formatPrice(value) {
  if (value >= 1000) return `$${(value / 1000).toFixed(1)}k`
  return `$${Number(value).toFixed(2)}`
}

const CustomTooltip = ({ active, payload, label }) => {
  if (!active || !payload?.length) return null
  const d = payload[0].payload
  return (
    <div className="chart-tooltip">
      <div className="tooltip-time">{formatTime(label)}</div>
      <div className="tooltip-row"><span>Close</span><span>${Number(d.close).toLocaleString('en-US', { minimumFractionDigits: 2 })}</span></div>
      <div className="tooltip-row"><span>Open</span><span>${Number(d.open).toFixed(2)}</span></div>
      <div className="tooltip-row"><span>High</span><span className="up">${Number(d.high).toFixed(2)}</span></div>
      <div className="tooltip-row"><span>Low</span><span className="down">${Number(d.low).toFixed(2)}</span></div>
      <div className="tooltip-row"><span>Vol</span><span>{Number(d.volume).toFixed(4)}</span></div>
    </div>
  )
}

export function PriceChart({ candles, ticker }) {
  const color = TICKER_COLORS[ticker] ?? '#00ff88'

  if (!candles || candles.length === 0) {
    return (
      <div className="chart-empty">
        <div className="chart-empty-inner">
          <div className="chart-pulse" style={{ '--color': color }} />
          <span>Waiting for {ticker} candles...</span>
        </div>
      </div>
    )
  }

  const data = candles.map(c => ({ ...c, time: c.windowStart }))
  const prices = data.map(d => d.close)
  const minP = Math.min(...prices)
  const maxP = Math.max(...prices)
  const pad = (maxP - minP) * 0.1 || 1

  return (
    <div className="chart-wrapper">
      <ResponsiveContainer width="100%" height="100%">
        <LineChart data={data} margin={{ top: 8, right: 16, left: 0, bottom: 0 }}>
          <defs>
            <linearGradient id={`grad-${ticker}`} x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stopColor={color} stopOpacity={0.3} />
              <stop offset="100%" stopColor={color} stopOpacity={0} />
            </linearGradient>
          </defs>
          <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.05)" />
          <XAxis
            dataKey="time"
            tickFormatter={formatTime}
            tick={{ fill: '#666', fontSize: 10, fontFamily: 'JetBrains Mono, monospace' }}
            tickLine={false}
            axisLine={{ stroke: 'rgba(255,255,255,0.08)' }}
            interval="preserveStartEnd"
          />
          <YAxis
            domain={[minP - pad, maxP + pad]}
            tickFormatter={formatPrice}
            tick={{ fill: '#666', fontSize: 10, fontFamily: 'JetBrains Mono, monospace' }}
            tickLine={false}
            axisLine={false}
            width={56}
          />
          <Tooltip content={<CustomTooltip />} />
          <Line
            type="monotone"
            dataKey="close"
            stroke={color}
            strokeWidth={2}
            dot={false}
            activeDot={{ r: 4, fill: color, strokeWidth: 0 }}
            isAnimationActive={false}
          />
        </LineChart>
      </ResponsiveContainer>
    </div>
  )
}