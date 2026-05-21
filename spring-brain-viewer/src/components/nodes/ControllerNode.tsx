import React from 'react'
import { BaseNode, type BaseNodeProps } from './BaseNode'

export function ControllerNode(props: BaseNodeProps) {
  return <BaseNode {...props} borderColor="#f0883e" textColor="#ffa657" />
}
