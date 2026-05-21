import type { NodeProps } from 'reactflow'
import { BaseNode } from './BaseNode'
import type { GraphNode } from '../../types'

export function ControllerNode(props: NodeProps<GraphNode>) {
  return <BaseNode {...props} borderColor="#f0883e" textColor="#ffa657" />
}
