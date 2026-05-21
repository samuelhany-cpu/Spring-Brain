import type { ReactNode } from 'react'
import { render, screen } from '@testing-library/react'
import { describe, it, expect, vi } from 'vitest'
import { GraphCanvas } from './GraphCanvas'
import type { Graph } from '../types'

vi.mock('reactflow', () => {
  const MockReactFlow = ({ nodes }: { nodes: { id: string }[] }) => (
    <div data-testid="react-flow">
      {nodes.map((n) => (
        <div key={n.id} data-testid="flow-node" />
      ))}
    </div>
  )
  return {
    default: MockReactFlow,
    ReactFlow: MockReactFlow,
    Background: () => null,
    Controls: () => null,
    MiniMap: () => null,
    ReactFlowProvider: ({ children }: { children: ReactNode }) => <>{children}</>,
    useNodesState: (init: unknown[]) => [init, vi.fn(), vi.fn()],
    useEdgesState: (init: unknown[]) => [init, vi.fn(), vi.fn()],
    Position: { Top: 'top', Bottom: 'bottom', Left: 'left', Right: 'right' },
    Handle: () => null,
    MarkerType: { ArrowClosed: 'arrowclosed' },
  }
})

vi.mock('@dagrejs/dagre', () => ({
  default: {
    graphlib: {
      Graph: class {
        setDefaultEdgeLabel() {}
        setGraph() {}
        setNode() {}
        setEdge() {}
        node() { return { x: 0, y: 0 } }
      },
    },
    layout: vi.fn(),
  },
}))

const mockGraph: Graph = {
  schemaVersion: '1.0.0',
  nodes: [
    { id: 'n1', type: 'controller', label: 'UserController', qualifiedName: 'com.example.UserController', file: 'UserController.java', line: 10 },
    { id: 'n2', type: 'service', label: 'UserService', qualifiedName: 'com.example.UserService', file: 'UserService.java', line: 5 },
    { id: 'n3', type: 'repository', label: 'UserRepository', qualifiedName: 'com.example.UserRepository', file: 'UserRepository.java', line: 3 },
  ],
  edges: [
    { id: 'e1', from: 'n1', to: 'n2', type: 'calls' },
    { id: 'e2', from: 'n2', to: 'n3', type: 'calls' },
  ],
}

describe('GraphCanvas', () => {
  it('renders the correct number of nodes', () => {
    render(
      <GraphCanvas
        graph={mockGraph}
        visibleTypes={new Set(['controller', 'service', 'repository'])}
        searchQuery=""
        onNodeClick={vi.fn()}
        selectedNodeId={null}
      />
    )
    expect(screen.getAllByTestId('flow-node')).toHaveLength(3)
  })

  it('filters nodes by visibleTypes', () => {
    render(
      <GraphCanvas
        graph={mockGraph}
        visibleTypes={new Set(['controller'])}
        searchQuery=""
        onNodeClick={vi.fn()}
        selectedNodeId={null}
      />
    )
    expect(screen.getAllByTestId('flow-node')).toHaveLength(1)
  })
})
