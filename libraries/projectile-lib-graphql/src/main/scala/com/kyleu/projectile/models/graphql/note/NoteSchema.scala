package com.kyleu.projectile.models.graphql.note

import com.kyleu.projectile.graphql.CommonSchema._
import com.kyleu.projectile.graphql.DateTimeSchema._
import com.kyleu.projectile.graphql.GraphQLUtils.deriveObjectType
import com.kyleu.projectile.graphql.{GraphQLContext, GraphQLSchemaHelper}
import com.kyleu.projectile.models.note.Note

import scala.concurrent.ExecutionContext

object NoteSchema extends GraphQLSchemaHelper("noteRow")(ExecutionContext.global) {
  implicit lazy val noteType: sangria.schema.ObjectType[GraphQLContext, Note] = deriveObjectType()
}
