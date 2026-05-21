import { useMemo } from 'react'
import ReactFlow, {
  Background,
  Controls,
  MiniMap,
  useNodesState,
  useEdgesState,
  MarkerType,
  type Node,
  type Edge,
} from 'reactflow'
import dagre from '@dagrejs/dagre'
import 'reactflow/dist/style.css'
import { nodeTypes } from './nodes'
import type { Graph, NodeType } from '../types'

interface Props {
  graph: Graph
  visibleTypes: Set<NodeType>
  searchQuery: string
  onNodeClick: (nodeId: string) => void
  selectedNodeId: string | null
}

function applyDagreLayout(nodes: Node[], edges: Edge[]): Node[] {
  const g = new dagre.graphlib.Graph()
  g.setDefaultEdgeLabel(() => ({}))
  g.setGraph({ rankdir: 'TB', ranksep: 60, nodesep: 40 })
  nodes.forEach((n) => g.setNode(n.id, { width: 120, height: 36 }))
  edges.forEach((e) => g.setEdge(e.source, e.target))
  dagre.layout(g)
  return nodes.map((n) => {
    const pos = g.node(n.id)
    return { ...n, position: { x: (pos?.x ?? 0) - 60, y: (pos?.y ?? 0) - 18 } }
  })
}

export function GraphCanvas({ graph, visibleTypes, searchQuery, onNodeClick, selectedNodeId }: Props) {
  const flowNodes: Node[] = useMemo(() => {
    const filtered = graph.nodes.filter((n) => visibleTypes.has(n.type))
    const raw: Node[] = filtered.map((n) => ({
      id: n.id,
      type: n.type,
      data: n,
      position: { x: 0, y: 0 },
      selected: n.id === selectedNodeId,
      style: {
        opacity: searchQuery && !n.label.toLowerCase().includes(searchQuery.toLowerCase()) ? 0.25 : 1,
      },
    }))
    const visibleIds = new Set(raw.map((n) => n.id))
    const filteredEdges: Edge[] = graph.edges
      .filter((e) => visibleIds.has(e.source) && visibleIds.has(e.target))
      .map((e) => ({
        id: e.id,
        source: e.source,
        target: e.target,
        markerEnd: { type: MarkerType.ArrowClosed, color: '#30363d' },
        style: { stroke: '#30363d' },
      }))
    return applyDagreLayout(raw, filteredEdges)
  }, [graph, visibleTypes, searchQuery, selectedNodeId])

  const flowEdges: Edge[] = useMemo(() => {
    const visibleIds = new Set(flowNodes.map((n) => n.id))
    return graph.edges
      .filter((e) => visibleIds.has(e.source) && visibleIds.has(e.target))
      .map((e) => ({
        id: e.id,
        source: e.source,
        target: e.target,
        markerEnd: { type: MarkerType.ArrowClosed, color: '#30363d' },
        style: { stroke: '#30363d' },
      }))
  }, [graph.edges, flowNodes])

  const [nodes, , onNodesChange] = useNodesState(flowNodes)
  const [edges, , onEdgesChange] = useEdgesState(flowEdges)

  return (
    <div style={{ flex: 1, height: '100%' }}>
      <ReactFlow
        nodes={nodes}
        edges={edges}
        nodeTypes={nodeTypes}
        onNodesChange={onNodesChange}
        onEdgesChange={onEdgesChange}
        onNodeClick={(_, node) => onNodeClick(node.id)}
        fitView
        attributionPosition="bottom-left"
      >
        <Background color="#30363d" gap={20} />
        <Controls style={{ background: '#161b22', border: '1px solid #30363d' }} />
        <MiniMap
          style={{ background: '#161b22', border: '1px solid #30363d' }}
          nodeColor="#30363d"
        />
      </ReactFlow>
    </div>
  )
}
