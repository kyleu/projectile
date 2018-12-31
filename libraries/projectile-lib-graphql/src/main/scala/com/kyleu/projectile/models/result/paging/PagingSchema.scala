package com.kyleu.projectile.models.result.paging

import com.kyleu.projectile.graphql.GraphQLContext
import sangria.macros.derive._
import sangria.schema.ObjectType

object PagingSchema {
  implicit val pagingOptionsRangeType: ObjectType[GraphQLContext, PagingOptions.Range] = deriveObjectType[GraphQLContext, PagingOptions.Range]()
  implicit val pagingOptionsType: ObjectType[GraphQLContext, PagingOptions] = deriveObjectType[GraphQLContext, PagingOptions]()
}
