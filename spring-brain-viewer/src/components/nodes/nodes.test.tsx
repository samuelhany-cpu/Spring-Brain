import React from 'react'
import { render, screen } from '@testing-library/react'
import { describe, it, expect, vi } from 'vitest'

vi.mock('reactflow', () => ({
  Handle: () => null,
  Position: { Top: 'top', Bottom: 'bottom' },
  ReactFlowProvider: ({ children }: { children: React.ReactNode }) => <>{children}</>,
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

describe('Node components', () => {
  it('RouteNode renders label', () => {
    render(<RouteNode id="n1" data={nodeData} type="route" selected={false} borderColor="#388bfd" textColor="#79c0ff" />)
    expect(screen.getByText('TestLabel')).toBeInTheDocument()
  })

  it('ControllerNode renders label', () => {
    render(<ControllerNode id="n1" data={nodeData} type="controller" selected={false} borderColor="#f0883e" textColor="#ffa657" />)
    expect(screen.getByText('TestLabel')).toBeInTheDocument()
  })

  it('ServiceNode renders label', () => {
    render(<ServiceNode id="n1" data={nodeData} type="service" selected={false} borderColor="#3fb950" textColor="#7ee787" />)
    expect(screen.getByText('TestLabel')).toBeInTheDocument()
  })

  it('RepositoryNode renders label', () => {
    render(<RepositoryNode id="n1" data={nodeData} type="repository" selected={false} borderColor="#bc8cff" textColor="#d2a8ff" />)
    expect(screen.getByText('TestLabel')).toBeInTheDocument()
  })

  it('EntityNode renders label', () => {
    render(<EntityNode id="n1" data={nodeData} type="entity" selected={false} borderColor="#f778ba" textColor="#f778ba" />)
    expect(screen.getByText('TestLabel')).toBeInTheDocument()
  })

  it('ConfigNode renders label', () => {
    render(<ConfigNode id="n1" data={nodeData} type="config_property" selected={false} borderColor="#e3b341" textColor="#e3b341" />)
    expect(screen.getByText('TestLabel')).toBeInTheDocument()
  })
})
