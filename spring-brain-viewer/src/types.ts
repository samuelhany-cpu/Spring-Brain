export type NodeType =
  | 'route'
  | 'controller'
  | 'service'
  | 'repository'
  | 'entity'
  | 'config_property'

export type LiveStatus = 'connected' | 'reconnecting'

export interface GraphNode {
  id: string
  type: NodeType
  label: string
  qualifiedName: string
  file: string
  line: number
  injects?: string[]
  routes?: string[]
  methods?: string[]
}

export interface GraphEdge {
  id: string
  from: string
  to: string
  type: string
}

export interface Graph {
  schemaVersion: string
  nodes: GraphNode[]
  edges: GraphEdge[]
}

export interface Diagnostic {
  severity: 'ERROR' | 'WARNING' | 'INFO'
  code: string
  message: string
  file: string
  line: number
  relatedNodeIds?: string[]
  suggestedFixes?: string[]
}

export interface DiagnosticsReport {
  schemaVersion: string
  diagnostics: Diagnostic[]
}
