package com.kyleu.projectile.graphql

import sangria.execution.deferred.DeferredResolver
import sangria.schema._

object EmptySchema extends GraphQLSchema {
  override val resolver = DeferredResolver.fetchers()

  private[this] val queryFields = fields[GraphQLContext, Unit]()

  override val queryType = ObjectType(
    name = "Query",
    description = "The main query interface",
    fields = queryFields.sortBy(_.name)
  )

  private[this] val mutationFields = fields[GraphQLContext, Unit]()

  override val mutationType = ObjectType(
    name = "Mutation",
    description = "The main mutation interface",
    fields = mutationFields.sortBy(_.name)
  )
}
