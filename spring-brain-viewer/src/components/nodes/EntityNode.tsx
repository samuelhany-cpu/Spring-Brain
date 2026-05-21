import React from 'react'
import { BaseNode, type BaseNodeProps } from './BaseNode'

export function EntityNode(props: BaseNodeProps) {
  return <BaseNode {...props} borderColor="#f778ba" textColor="#f778ba" hasSource={false} />
}
