import { RouteNode } from './RouteNode'
import { ControllerNode } from './ControllerNode'
import { ServiceNode } from './ServiceNode'
import { RepositoryNode } from './RepositoryNode'
import { EntityNode } from './EntityNode'
import { ConfigNode } from './ConfigNode'

export const nodeTypes = {
  route: RouteNode,
  controller: ControllerNode,
  service: ServiceNode,
  repository: RepositoryNode,
  entity: EntityNode,
  config_property: ConfigNode,
}

export { RouteNode, ControllerNode, ServiceNode, RepositoryNode, EntityNode, ConfigNode }
