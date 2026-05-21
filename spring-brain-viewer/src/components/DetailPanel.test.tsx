import React from 'react'
import { render, screen } from '@testing-library/react'
import { describe, it, expect } from 'vitest'
import { DetailPanel } from './DetailPanel'
import type { Graph, DiagnosticsReport } from '../types'

const graph: Graph = {
  schemaVersion: '1.0.0',
  nodes: [
    {
      id: 'controller:com.example.UserController',
      type: 'controller',
      label: 'UserController',
      qualifiedName: 'com.example.UserController',
      file: 'src/main/java/com/example/UserController.java',
      line: 22,
      injects: ['UserService'],
      routes: ['GET /api/users', 'POST /api/users'],
    },
  ],
  edges: [],
}

const diagnostics: DiagnosticsReport = {
  schemaVersion: '1.0.0',
  diagnostics: [
    {
      severity: 'WARNING',
      code: 'DIRECT_REPO',
      message: 'Direct repository access',
      file: 'src/main/java/com/example/UserController.java',
      line: 9,
    },
  ],
}

describe('DetailPanel', () => {
  it('shows node name and type when node is selected', () => {
    render(
      <DetailPanel
        selectedNodeId="controller:com.example.UserController"
        graph={graph}
        diagnostics={diagnostics}
        liveStatus="connected"
      />
    )
    expect(screen.getByText('UserController')).toBeInTheDocument()
    expect(screen.getByText('controller')).toBeInTheDocument()
  })

  it('shows injects list for selected controller', () => {
    render(
      <DetailPanel
        selectedNodeId="controller:com.example.UserController"
        graph={graph}
        diagnostics={diagnostics}
        liveStatus="connected"
      />
    )
    expect(screen.getByText('→ UserService')).toBeInTheDocument()
  })

  it('shows routes for selected controller', () => {
    render(
      <DetailPanel
        selectedNodeId="controller:com.example.UserController"
        graph={graph}
        diagnostics={diagnostics}
        liveStatus="connected"
      />
    )
    expect(screen.getByText('GET /api/users')).toBeInTheDocument()
    expect(screen.getByText('POST /api/users')).toBeInTheDocument()
  })

  it('shows diagnostics scoped to selected node file', () => {
    render(
      <DetailPanel
        selectedNodeId="controller:com.example.UserController"
        graph={graph}
        diagnostics={diagnostics}
        liveStatus="connected"
      />
    )
    expect(screen.getByText('Direct repository access')).toBeInTheDocument()
  })

  it('shows live status indicator when nothing selected', () => {
    render(
      <DetailPanel
        selectedNodeId={null}
        graph={graph}
        diagnostics={diagnostics}
        liveStatus="connected"
      />
    )
    expect(screen.getByText('● Live — watching for changes')).toBeInTheDocument()
  })
})
