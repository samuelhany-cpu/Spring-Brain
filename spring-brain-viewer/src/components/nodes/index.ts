import { RouteNode } from './RouteNode'
import { ControllerNode } from './ControllerNode'
import { ServiceNode } from './ServiceNode'
import { RepositoryNode } from './RepositoryNode'
import { EntityNode } from './EntityNode'
import { ConfigNode } from './ConfigNode'
import { BeanNode } from './BeanNode'

export const nodeTypes = {
  route: RouteNode,
  controller: ControllerNode,
  service: ServiceNode,
  repository: RepositoryNode,
  entity: EntityNode,
  config_property: ConfigNode,
  bean: BeanNode,
}

export { RouteNode, ControllerNode, ServiceNode, RepositoryNode, EntityNode, ConfigNode, BeanNode }
