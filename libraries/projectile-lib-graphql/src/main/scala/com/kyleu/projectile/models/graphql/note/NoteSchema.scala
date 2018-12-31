package com.kyleu.projectile.models.graphql.note

import com.kyleu.projectile.graphql.CommonSchema._
import com.kyleu.projectile.graphql.DateTimeSchema._
import com.kyleu.projectile.graphql.GraphQLUtils.deriveObjectType
import com.kyleu.projectile.graphql.{GraphQLContext, GraphQLSchemaHelper}
import com.kyleu.projectile.models.note.Note

object NoteSchema extends GraphQLSchemaHelper("noteRow") {
  implicit lazy val noteType: sangria.schema.ObjectType[GraphQLContext, Note] = deriveObjectType()
}
