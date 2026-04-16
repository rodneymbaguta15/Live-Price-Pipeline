const BASE_URL = 'http://localhost:8080/api'

/**
 * Fetches the most recent N candles for a ticker from the REST API.
 * Called on mount to pre-populate the chart before the WebSocket stream starts.
 * API returns newest-first — we reverse to oldest-first for Recharts.
 */
export async function fetchCandles(ticker, limit = 60) {
  const res = await fetch(`${BASE_URL}/candles/${ticker}?limit=${limit}`)
  if (!res.ok) throw new Error(`Failed to fetch candles for ${ticker}: ${res.status}`)
  const data = await res.json()
  return data.reverse()
}