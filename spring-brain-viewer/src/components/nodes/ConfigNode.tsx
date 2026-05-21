import React from 'react'
import { BaseNode, type BaseNodeProps } from './BaseNode'

export function ConfigNode(props: BaseNodeProps) {
  return <BaseNode {...props} borderColor="#e3b341" textColor="#e3b341" hasSource={false} />
}
