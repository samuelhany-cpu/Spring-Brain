import type { NodeProps } from 'reactflow'
import { BaseNode } from './BaseNode'
import type { GraphNode } from '../../types'

export function RepositoryNode(props: NodeProps<GraphNode>) {
  return <BaseNode {...props} borderColor="#bc8cff" textColor="#d2a8ff" />
}
