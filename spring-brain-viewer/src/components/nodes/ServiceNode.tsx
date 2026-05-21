import React from 'react'
import { BaseNode, type BaseNodeProps } from './BaseNode'

export function ServiceNode(props: BaseNodeProps) {
  return <BaseNode {...props} borderColor="#3fb950" textColor="#7ee787" />
}
