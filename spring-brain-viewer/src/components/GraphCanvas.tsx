import { useMemo, useEffect } from 'react'
import ReactFlow, {
  Background,
  Controls,
  MiniMap,
  useNodesState,
  useEdgesState,
  useReactFlow,
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

const NODE_HEIGHT = 40
const CHAR_WIDTH = 7.5 // approximate px per character at 11px font
const H_PAD = 24      // horizontal padding inside node

function nodeWidth(label: string): number {
  return Math.max(120, Math.ceil(label.length * CHAR_WIDTH) + H_PAD)
}

function applyDagreLayout(nodes: Node[], edges: Edge[]): Node[] {
  const g = new dagre.graphlib.Graph()
  g.setDefaultEdgeLabel(() => ({}))
  g.setGraph({ rankdir: 'LR', ranksep: 100, nodesep: 12 })
  nodes.forEach((n) => {
    const w = nodeWidth((n.data as { label: string }).label ?? n.id)
    g.setNode(n.id, { width: w, height: NODE_HEIGHT })
  })
  edges.forEach((e) => g.setEdge(e.source, e.target))
  dagre.layout(g)
  return nodes.map((n) => {
    const pos = g.node(n.id)
    const w = nodeWidth((n.data as { label: string }).label ?? n.id)
    return { ...n, position: { x: (pos?.x ?? 0) - w / 2, y: (pos?.y ?? 0) - NODE_HEIGHT / 2 } }
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
      .filter((e) => visibleIds.has(e.from) && visibleIds.has(e.to))
      .map((e) => ({
        id: e.id,
        source: e.from,
        target: e.to,
        markerEnd: { type: MarkerType.ArrowClosed, color: '#30363d' },
        style: { stroke: '#30363d' },
      }))
    return applyDagreLayout(raw, filteredEdges)
  }, [graph, visibleTypes, searchQuery, selectedNodeId])

  const flowEdges: Edge[] = useMemo(() => {
    const visibleIds = new Set(flowNodes.map((n) => n.id))
    return graph.edges
      .filter((e) => visibleIds.has(e.from) && visibleIds.has(e.to))
      .map((e) => ({
        id: e.id,
        source: e.from,
        target: e.to,
        markerEnd: { type: MarkerType.ArrowClosed, color: '#30363d' },
        style: { stroke: '#30363d' },
      }))
  }, [graph.edges, flowNodes])

  // Changes to graph structure or visible types should re-fit the viewport.
  // Search query only dims nodes — no re-fit needed there.
  const layoutKey = useMemo(
    () => graph.nodes.map((n) => n.id).sort().join(',') + '|' + [...visibleTypes].sort().join(','),
    [graph.nodes, visibleTypes],
  )

  const { fitView } = useReactFlow()

  const [nodes, setNodes, onNodesChange] = useNodesState(flowNodes)
  const [edges, setEdges, onEdgesChange] = useEdgesState(flowEdges)

  useEffect(() => { setNodes(flowNodes) }, [flowNodes, setNodes])
  useEffect(() => { setEdges(flowEdges) }, [flowEdges, setEdges])

  // Re-fit after layout settles so all nodes are visible regardless of screen size.
  useEffect(() => {
    const id = setTimeout(() => fitView({ padding: 0.08, duration: 350 }), 60)
    return () => clearTimeout(id)
  }, [layoutKey, fitView])

  return (
    <div style={{ flex: 1, height: '100%' }}>
      <ReactFlow
        nodes={nodes}
        edges={edges}
        nodeTypes={nodeTypes}
        onNodesChange={onNodesChange}
        onEdgesChange={onEdgesChange}
        onNodeClick={(_, node) => {
          onNodeClick(node.id)
          fitView({ nodes: [{ id: node.id }], duration: 400, padding: 0.6 })
        }}
        fitView
        fitViewOptions={{ padding: 0.08 }}
        minZoom={0.05}
        maxZoom={2}
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
