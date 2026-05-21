import type { ReactNode } from 'react'
import { render, screen } from '@testing-library/react'
import { describe, it, expect, vi } from 'vitest'

vi.mock('reactflow', () => ({
  Handle: () => null,
  Position: { Top: 'top', Bottom: 'bottom' },
  ReactFlowProvider: ({ children }: { children: ReactNode }) => <>{children}</>,
}))

import { RouteNode } from './RouteNode'
import { ControllerNode } from './ControllerNode'
import { ServiceNode } from './ServiceNode'
import { RepositoryNode } from './RepositoryNode'
import { EntityNode } from './EntityNode'
import { ConfigNode } from './ConfigNode'

const nodeData = {
  label: 'TestLabel',
  qualifiedName: 'com.example.TestLabel',
  file: 'Test.java',
  line: 10,
}

const baseProps = { id: 'n1', data: nodeData, selected: false }

describe('Node components', () => {
  it('RouteNode renders label', () => {
    render(<RouteNode {...({ ...baseProps, type: 'route' } as any)} />)
    expect(screen.getByText('TestLabel')).toBeInTheDocument()
  })

  it('ControllerNode renders label', () => {
    render(<ControllerNode {...({ ...baseProps, type: 'controller' } as any)} />)
    expect(screen.getByText('TestLabel')).toBeInTheDocument()
  })

  it('ServiceNode renders label', () => {
    render(<ServiceNode {...({ ...baseProps, type: 'service' } as any)} />)
    expect(screen.getByText('TestLabel')).toBeInTheDocument()
  })

  it('RepositoryNode renders label', () => {
    render(<RepositoryNode {...({ ...baseProps, type: 'repository' } as any)} />)
    expect(screen.getByText('TestLabel')).toBeInTheDocument()
  })

  it('EntityNode renders label', () => {
    render(<EntityNode {...({ ...baseProps, type: 'entity' } as any)} />)
    expect(screen.getByText('TestLabel')).toBeInTheDocument()
  })

  it('ConfigNode renders label', () => {
    render(<ConfigNode {...({ ...baseProps, type: 'config_property' } as any)} />)
    expect(screen.getByText('TestLabel')).toBeInTheDocument()
  })
})
