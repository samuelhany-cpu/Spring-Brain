import type { NodeProps } from 'reactflow'
import { BaseNode } from './BaseNode'
import type { GraphNode } from '../../types'

export function BeanNode(props: NodeProps<GraphNode>) {
  return <BaseNode {...props} borderColor="#8b949e" textColor="#b1bac4" />
}
