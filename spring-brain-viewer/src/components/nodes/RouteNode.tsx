import type { NodeProps } from 'reactflow'
import { BaseNode } from './BaseNode'
import type { GraphNode } from '../../types'

export function RouteNode(props: NodeProps<GraphNode>) {
  return <BaseNode {...props} borderColor="#388bfd" textColor="#79c0ff" hasTarget={false} />
}
