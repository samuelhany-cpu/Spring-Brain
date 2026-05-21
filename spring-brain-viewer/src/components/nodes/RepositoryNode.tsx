import React from 'react'
import { BaseNode, type BaseNodeProps } from './BaseNode'

export function RepositoryNode(props: BaseNodeProps) {
  return <BaseNode {...props} borderColor="#bc8cff" textColor="#d2a8ff" />
}
