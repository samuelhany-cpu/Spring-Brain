import { useState, useMemo } from 'react'
import { ReactFlowProvider } from 'reactflow'
import { useGraphData } from './hooks/useGraphData'
import { Toolbar } from './components/Toolbar'
import { GraphCanvas } from './components/GraphCanvas'
import { DetailPanel } from './components/DetailPanel'
import { BrainBackground } from './components/BrainBackground'
import type { NodeType } from './types'

const ALL_TYPES = new Set<NodeType>(['route', 'controller', 'service', 'repository', 'entity', 'config_property'])

export default function App() {
  const { graph, diagnostics, liveStatus } = useGraphData()
  const [visibleTypes, setVisibleTypes] = useState<Set<NodeType>>(new Set(ALL_TYPES))
  const [searchQuery, setSearchQuery] = useState('')
  const [selectedNodeId, setSelectedNodeId] = useState<string | null>(null)

  const nodeCount = useMemo(
    () => graph?.nodes.filter((n) => visibleTypes.has(n.type)).length ?? 0,
    [graph, visibleTypes],
  )
  const edgeCount = useMemo(() => graph?.edges.length ?? 0, [graph])

  if (!graph) {
    return (
      <div className="flex items-center justify-center h-screen text-[#8b949e]">
        Loading…
      </div>
    )
  }

  return (
    <ReactFlowProvider>
      <BrainBackground />
      <div className="flex flex-col h-screen" style={{ position: 'relative', zIndex: 1 }}>
        <Toolbar
          nodeCount={nodeCount}
          edgeCount={edgeCount}
          diagnostics={diagnostics}
          visibleTypes={visibleTypes}
          searchQuery={searchQuery}
          onVisibleTypesChange={setVisibleTypes}
          onSearchChange={setSearchQuery}
        />
        <div className="flex flex-1 overflow-hidden">
          <GraphCanvas
            graph={graph}
            visibleTypes={visibleTypes}
            searchQuery={searchQuery}
            onNodeClick={setSelectedNodeId}
            selectedNodeId={selectedNodeId}
          />
          <DetailPanel
            selectedNodeId={selectedNodeId}
            graph={graph}
            diagnostics={diagnostics}
            liveStatus={liveStatus}
          />
        </div>
      </div>
    </ReactFlowProvider>
  )
}
