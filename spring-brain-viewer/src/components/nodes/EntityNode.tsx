import type { NodeProps } from 'reactflow'
import { BaseNode } from './BaseNode'
import type { GraphNode } from '../../types'

export function EntityNode(props: NodeProps<GraphNode>) {
  return <BaseNode {...props} borderColor="#f778ba" textColor="#f778ba" hasSource={false} />
}
