import type { NodeProps } from 'reactflow'
import { BaseNode } from './BaseNode'
import type { GraphNode } from '../../types'

export function ConfigNode(props: NodeProps<GraphNode>) {
  return <BaseNode {...props} borderColor="#e3b341" textColor="#e3b341" hasSource={false} />
}
