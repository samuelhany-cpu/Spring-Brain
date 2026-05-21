import React from 'react'
import type { Graph, DiagnosticsReport, LiveStatus } from '../types'

interface Props {
  selectedNodeId: string | null
  graph: Graph | null
  diagnostics: DiagnosticsReport | null
  liveStatus: LiveStatus
}

const TYPE_COLORS: Record<string, string> = {
  route: '#79c0ff',
  controller: '#ffa657',
  service: '#7ee787',
  repository: '#d2a8ff',
  entity: '#f778ba',
  config_property: '#e3b341',
}

export function DetailPanel({ selectedNodeId, graph, diagnostics, liveStatus }: Props) {
  const selectedNode = selectedNodeId ? graph?.nodes.find((n) => n.id === selectedNodeId) : null

  const scopedDiagnostics = diagnostics?.diagnostics.filter((d) => {
    if (selectedNode) {
      const nodeFileName = selectedNode.file.split(/[/\\]/).pop() ?? ''
      return d.file.includes(nodeFileName)
    }
    return true
  }) ?? []

  const typeColor = selectedNode ? (TYPE_COLORS[selectedNode.type] ?? '#e6edf3') : '#e6edf3'
  const label8b = 'text-[#8b949e] text-[9px] uppercase tracking-widest mb-1 mt-2.5'

  return (
    <div className="w-[220px] border-l border-[#30363d] flex flex-col bg-[#0d1117] text-[#e6edf3] overflow-y-auto">
      {selectedNode ? (
        <div className="p-3 border-b border-[#30363d]">
          <div className={label8b}>Selected Node</div>
          <div className="font-semibold text-[13px] mb-1" style={{ color: typeColor }}>{selectedNode.label}</div>
          <span className="inline-block bg-[#21262d] rounded px-2 py-0.5 text-[10px] mb-2" style={{ color: typeColor }}>
            {selectedNode.type}
          </span>
          <div className="text-[#8b949e] text-[10px] mb-1">
            {selectedNode.qualifiedName.split('.').slice(0, -1).join('.')}
          </div>
          <div className="text-[#58a6ff] text-[10px] cursor-pointer">
            {selectedNode.file.split(/[/\\]/).pop()}:{selectedNode.line} ↗
          </div>

          {selectedNode.injects && selectedNode.injects.length > 0 && (
            <>
              <div className={label8b}>Injects</div>
              {selectedNode.injects.map((s) => (
                <div key={s} className="text-[#7ee787] text-[10px]">→ {s}</div>
              ))}
            </>
          )}

          {selectedNode.routes && selectedNode.routes.length > 0 && (
            <>
              <div className={label8b}>Routes ({selectedNode.routes.length})</div>
              {selectedNode.routes.map((r) => (
                <div key={r} className="text-[#79c0ff] text-[10px]">{r}</div>
              ))}
            </>
          )}
        </div>
      ) : null}

      <div className="p-3 flex-1">
        <div className={label8b}>Diagnostics</div>
        {scopedDiagnostics.length === 0 ? (
          <div className="text-[#8b949e] text-[10px]">No issues</div>
        ) : (
          scopedDiagnostics.map((d, i) => (
            <div key={i} className="bg-[#3d1f1f] border border-[#f85149] rounded p-2 mb-2">
              <div className="text-[#f85149] text-[9px] mb-1">⚠ {d.severity}</div>
              <div className="text-[10px] mb-1">{d.message}</div>
              <div className="text-[#8b949e] text-[9px]">{d.file.split(/[/\\]/).pop()}:{d.line}</div>
            </div>
          ))
        )}

        {!selectedNode && (
          <div className={`text-[10px] text-center mt-2.5 ${liveStatus === 'connected' ? 'text-[#3fb950]' : 'text-[#8b949e]'}`}>
            ● {liveStatus === 'connected' ? 'Live — watching for changes' : 'Reconnecting…'}
          </div>
        )}
      </div>
    </div>
  )
}
