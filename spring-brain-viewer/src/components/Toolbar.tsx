import React from 'react'
import type { DiagnosticsReport, NodeType } from '../types'

const ALL_TYPES: NodeType[] = ['route', 'controller', 'service', 'repository', 'entity', 'config_property']
const CHIP_LABELS: Record<string, string> = {
  route: 'Routes',
  controller: 'Controllers',
  service: 'Services',
  repository: 'Repos',
  entity: 'Entities',
  config_property: 'Configs',
}

interface Props {
  nodeCount: number
  edgeCount: number
  diagnostics: DiagnosticsReport | null
  visibleTypes: Set<NodeType>
  searchQuery: string
  onVisibleTypesChange: (types: Set<NodeType>) => void
  onSearchChange: (query: string) => void
}

export function Toolbar({ nodeCount, edgeCount, diagnostics, visibleTypes, searchQuery, onVisibleTypesChange, onSearchChange }: Props) {
  const warningCount = diagnostics?.diagnostics.filter((d) => d.severity === 'WARNING').length ?? 0
  const errorCount = diagnostics?.diagnostics.filter((d) => d.severity === 'ERROR').length ?? 0
  const allVisible = ALL_TYPES.every((t) => visibleTypes.has(t))

  function toggleAll() {
    onVisibleTypesChange(new Set(ALL_TYPES))
  }

  function toggleType(type: NodeType) {
    const next = new Set(visibleTypes)
    if (next.has(type)) next.delete(type)
    else next.add(type)
    onVisibleTypesChange(next)
  }

  const chipBase = 'rounded-full px-2.5 py-0.5 text-xs cursor-pointer border'

  return (
    <div className="flex items-center gap-3 flex-wrap px-3.5 py-2 border-b border-[#30363d] bg-[#161b22]">
      <span className="text-[#58a6ff] font-semibold text-sm">⬡ spring-brain</span>
      <span className="text-[#30363d]">|</span>

      <div className="flex gap-1.5">
        <button
          className={`${chipBase} ${allVisible ? 'bg-[#21262d] text-[#3fb950] border-[#3fb950]' : 'bg-[#21262d] text-[#8b949e] border-[#30363d]'}`}
          onClick={toggleAll}
        >
          All
        </button>
        {ALL_TYPES.map((type) => (
          <button
            key={type}
            className={`${chipBase} ${visibleTypes.has(type) && !allVisible ? 'bg-[#21262d] text-[#3fb950] border-[#3fb950]' : 'bg-[#21262d] text-[#8b949e] border-[#30363d]'}`}
            onClick={() => toggleType(type)}
          >
            {CHIP_LABELS[type]}
          </button>
        ))}
      </div>

      <input
        className="bg-[#0d1117] border border-[#30363d] rounded-md px-2.5 py-1 text-[#e6edf3] text-xs w-40"
        placeholder="Search class, route…"
        value={searchQuery}
        onChange={(e) => onSearchChange(e.target.value)}
      />

      <div className="ml-auto flex items-center gap-2.5">
        {errorCount > 0 && (
          <span className="bg-[#3d1f1f] text-[#f85149] border border-[#f85149] rounded-full px-2.5 py-0.5 text-xs">
            ✖ {errorCount} errors
          </span>
        )}
        {warningCount > 0 && (
          <span className="bg-[#3d1f1f] text-[#f85149] border border-[#f85149] rounded-full px-2.5 py-0.5 text-xs">
            ⚠ {warningCount} warnings
          </span>
        )}
        <span className="text-[#8b949e] text-xs">{nodeCount} nodes · {edgeCount} edges</span>
      </div>
    </div>
  )
}
