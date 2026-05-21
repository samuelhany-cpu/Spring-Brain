import React from 'react'
import { BaseNode, type BaseNodeProps } from './BaseNode'

export function RouteNode(props: BaseNodeProps) {
  return <BaseNode {...props} borderColor="#388bfd" textColor="#79c0ff" hasTarget={false} />
}
