import type { NodeProps } from 'reactflow'
import { BaseNode } from './BaseNode'
import type { GraphNode } from '../../types'

export function ServiceNode(props: NodeProps<GraphNode>) {
  return <BaseNode {...props} borderColor="#3fb950" textColor="#7ee787" />
}
