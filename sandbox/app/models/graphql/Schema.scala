package models.graphql

import com.kyleu.projectile.graphql.{GraphQLContext, GraphQLSchema}
import sangria.execution.deferred.DeferredResolver
import sangria.schema._
import util.Version

import scala.concurrent.Future

object Schema extends GraphQLSchema {
  override val resolver = DeferredResolver.fetchers()

  private[this] val queryFields = fields[GraphQLContext, Unit](
    Field(name = "status", fieldType = StringType, resolve = _ => Future.successful("OK")),
    Field(name = "version", fieldType = StringType, resolve = _ => Future.successful(Version.version))
  )

  override val queryType = ObjectType(
    name = "Query",
    description = "The main query interface.",
    fields = queryFields.sortBy(_.name)
  )

  private[this] val mutationFields = fields[GraphQLContext, Unit]()

  override val mutationType = ObjectType(
    name = "Mutation",
    description = "The main mutation interface.",
    fields = mutationFields.sortBy(_.name)
  )
}
