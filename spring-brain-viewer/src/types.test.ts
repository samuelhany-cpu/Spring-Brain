import { describe, it, expect } from 'vitest'
import type { GraphNode, Graph, DiagnosticsReport, Diagnostic, NodeType, LiveStatus } from './types'

describe('types', () => {
  it('NodeType covers all node kinds', () => {
    const types: NodeType[] = ['route', 'controller', 'service', 'repository', 'entity', 'config_property']
    expect(types).toHaveLength(6)
  })

  it('GraphNode can be constructed with required fields', () => {
    const node: GraphNode = {
      id: 'controller:com.example.UserController',
      type: 'controller',
      label: 'UserController',
      qualifiedName: 'com.example.UserController',
      file: 'UserController.java',
      line: 10,
    }
    expect(node.id).toBe('controller:com.example.UserController')
  })

  it('Diagnostic can be constructed with required fields', () => {
    const d: Diagnostic = {
      severity: 'WARNING',
      code: 'DIRECT_REPO',
      message: 'Direct repository access',
      file: 'ItemController.java',
      line: 9,
    }
    expect(d.severity).toBe('WARNING')
  })

  it('Graph schema version field exists', () => {
    const g: Graph = { schemaVersion: '1.0.0', nodes: [], edges: [] }
    expect(g.schemaVersion).toBe('1.0.0')
  })

  it('DiagnosticsReport schema version field exists', () => {
    const r: DiagnosticsReport = { schemaVersion: '1.0.0', diagnostics: [] }
    expect(r.schemaVersion).toBe('1.0.0')
  })

  it('LiveStatus accepts connected and reconnecting', () => {
    const a: LiveStatus = 'connected'
    const b: LiveStatus = 'reconnecting'
    expect([a, b]).toHaveLength(2)
  })
})
