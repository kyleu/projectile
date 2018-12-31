/* Generated File */
package com.kyleu.projectile.models.note

import com.kyleu.projectile.graphql.{GraphQLContext, GraphQLSchemaHelper}
import com.kyleu.projectile.graphql.GraphQLUtils._

object NoteSchema extends GraphQLSchemaHelper("noteRow") {
  implicit lazy val noteType: sangria.schema.ObjectType[GraphQLContext, Note] = deriveObjectType()
}
