package com.kyleu.projectile.graphql

import sangria.execution.deferred.DeferredResolver
import sangria.schema.ObjectType

trait GraphQLSchema {
  def resolver: DeferredResolver[GraphQLContext]

  def queryType: ObjectType[GraphQLContext, Unit]
  def mutationType: ObjectType[GraphQLContext, Unit]

  // Schema
  lazy val schema = sangria.schema.Schema(
    query = queryType,
    mutation = if (mutationType.fields.isEmpty) { None } else { Some(mutationType) },
    subscription = None,
    additionalTypes = Nil
  )
}
