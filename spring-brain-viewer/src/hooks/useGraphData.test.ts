import { renderHook, waitFor } from '@testing-library/react'
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { useGraphData } from './useGraphData'
import type { Graph, DiagnosticsReport } from '../types'

const mockGraph: Graph = {
  schemaVersion: '1.0.0',
  nodes: [
    {
      id: 'controller:com.example.UserController',
      type: 'controller',
      label: 'UserController',
      qualifiedName: 'com.example.UserController',
      file: 'UserController.java',
      line: 10,
    },
  ],
  edges: [],
}

const mockDiagnostics: DiagnosticsReport = {
  schemaVersion: '1.0.0',
  diagnostics: [],
}

describe('useGraphData', () => {
  beforeEach(() => {
    vi.stubGlobal('fetch', vi.fn((url: string) => {
      if (url === '/api/graph')
        return Promise.resolve({ ok: true, json: () => Promise.resolve(mockGraph) })
      if (url === '/api/diagnostics')
        return Promise.resolve({ ok: true, json: () => Promise.resolve(mockDiagnostics) })
      return Promise.reject(new Error(`Unknown URL: ${url}`))
    }))

    const mockEventSource = {
      addEventListener: vi.fn(),
      close: vi.fn(),
    }
    vi.stubGlobal('EventSource', vi.fn(() => mockEventSource))
  })

  afterEach(() => {
    vi.unstubAllGlobals()
  })

  it('fetches graph and diagnostics on mount', async () => {
    const { result } = renderHook(() => useGraphData())
    await waitFor(() => expect(result.current.graph).not.toBeNull())
    expect(result.current.graph?.nodes).toHaveLength(1)
    expect(result.current.diagnostics?.diagnostics).toHaveLength(0)
  })

  it('opens an EventSource to /api/events', async () => {
    renderHook(() => useGraphData())
    await waitFor(() => expect(EventSource).toHaveBeenCalledWith('/api/events'))
  })

  it('re-fetches when SSE refresh event fires', async () => {
    let onMessage: ((e: MessageEvent) => void) | null = null
    const mockEventSource = {
      addEventListener: vi.fn((event: string, handler: (e: MessageEvent) => void) => {
        if (event === 'message') onMessage = handler
      }),
      close: vi.fn(),
    }
    vi.stubGlobal('EventSource', vi.fn(() => mockEventSource))

    const { result } = renderHook(() => useGraphData())
    await waitFor(() => expect(result.current.graph).not.toBeNull())

    const callsBefore = (fetch as ReturnType<typeof vi.fn>).mock.calls.length
    onMessage!({ data: 'refresh' } as MessageEvent)
    await waitFor(() => {
      expect((fetch as ReturnType<typeof vi.fn>).mock.calls.length).toBeGreaterThan(callsBefore)
    })
  })

  it('returns liveStatus connected when EventSource open fires', async () => {
    const mockEventSource = {
      addEventListener: vi.fn((event: string, handler: () => void) => {
        if (event === 'open') setTimeout(handler, 0)
      }),
      close: vi.fn(),
    }
    vi.stubGlobal('EventSource', vi.fn(() => mockEventSource))
    const { result } = renderHook(() => useGraphData())
    await waitFor(() => expect(result.current.liveStatus).toBe('connected'))
  })
})
