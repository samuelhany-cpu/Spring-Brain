import { Handle, Position } from 'reactflow'
import type { NodeProps } from 'reactflow'
import type { GraphNode } from '../../types'

export interface BaseNodeColors {
  borderColor: string
  textColor: string
  hasTarget?: boolean
  hasSource?: boolean
}

export type GraphNodeProps = NodeProps<GraphNode> & BaseNodeColors

export function BaseNode({ data, selected, borderColor, textColor, hasTarget = true, hasSource = true }: GraphNodeProps) {
  return (
    <div
      style={{
        background: '#1c2128',
        border: `${selected ? '2px' : '1px'} solid ${borderColor}`,
        borderRadius: 6,
        padding: '5px 10px',
        fontSize: 11,
        color: textColor,
        boxShadow: selected ? `0 0 8px ${borderColor}66` : undefined,
        minWidth: 80,
        textAlign: 'center',
      }}
    >
      {hasTarget && <Handle type="target" position={Position.Top} style={{ background: borderColor }} />}
      <span>{data.label}</span>
      {hasSource && <Handle type="source" position={Position.Bottom} style={{ background: borderColor }} />}
    </div>
  )
}
