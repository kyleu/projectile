package com.kyleu.projectile.models.graphql

import sangria.schema._

import scala.concurrent.Future

object EmptySchema extends BaseGraphQLSchema {
  override protected def additionalQueryFields = fields(
    Field(name = "status", fieldType = StringType, resolve = _ => Future.successful("OK"))
  )
}
