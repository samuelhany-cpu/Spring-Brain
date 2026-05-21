import { useState, useEffect, useCallback } from 'react'
import type { Graph, DiagnosticsReport, LiveStatus } from '../types'

interface GraphData {
  graph: Graph | null
  diagnostics: DiagnosticsReport | null
  liveStatus: LiveStatus
}

export function useGraphData(): GraphData {
  const [graph, setGraph] = useState<Graph | null>(null)
  const [diagnostics, setDiagnostics] = useState<DiagnosticsReport | null>(null)
  const [liveStatus, setLiveStatus] = useState<LiveStatus>('reconnecting')

  const fetchData = useCallback(async () => {
    const [graphRes, diagRes] = await Promise.all([
      fetch('/api/graph'),
      fetch('/api/diagnostics'),
    ])
    if (graphRes.ok) setGraph(await graphRes.json())
    if (diagRes.ok) setDiagnostics(await diagRes.json())
  }, [])

  useEffect(() => {
    fetchData()
    const es = new EventSource('/api/events')
    es.addEventListener('open', () => setLiveStatus('connected'))
    es.addEventListener('error', () => setLiveStatus('reconnecting'))
    es.addEventListener('message', (e: MessageEvent) => {
      if (e.data === 'refresh') fetchData()
    })
    return () => es.close()
  }, [fetchData])

  return { graph, diagnostics, liveStatus }
}
