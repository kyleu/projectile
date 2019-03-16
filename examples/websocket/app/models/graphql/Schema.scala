package models.graphql

import com.kyleu.projectile.graphql.{GraphQLContext, GraphQLSchema}
import sangria.execution.deferred.DeferredResolver
import sangria.schema._

import scala.concurrent.Future

object Schema extends GraphQLSchema {
  // Fetchers
  private[this] val modelFetchers = {
    Nil ++
      /* Start model fetchers */
      /* Projectile export section [websocket] */
      Seq(
        models.graphql.auth.Oauth2InfoRowSchema.oauth2InfoRowByPrimaryKeyFetcher,
        models.graphql.auth.PasswordInfoRowSchema.passwordInfoRowByPrimaryKeyFetcher,
        models.graphql.auth.SystemUserRowSchema.systemUserRowByPrimaryKeyFetcher,
        models.graphql.note.NoteRowSchema.noteRowByAuthorFetcher,
        models.graphql.note.NoteRowSchema.noteRowByPrimaryKeyFetcher
      ) ++
        /* End model fetchers */
        Nil
  }

  override val resolver = DeferredResolver.fetchers(modelFetchers: _*)

  private[this] val customQueryFields = fields[GraphQLContext, Unit](
    Field(name = "status", fieldType = StringType, resolve = _ => Future.successful("OK")),
    Field(name = "version", fieldType = StringType, resolve = _ => Future.successful("0.0.1"))
  )

  // Query Types
  private[this] val baseQueryFields = customQueryFields

  private[this] val modelQueryFields: Seq[Field[GraphQLContext, Unit]] = {
    Nil ++
      /* Start model query fields */
      /* Projectile export section [websocket] */
      models.graphql.auth.Oauth2InfoRowSchema.queryFields ++
      models.graphql.auth.PasswordInfoRowSchema.queryFields ++
      models.graphql.auth.SystemUserRowSchema.queryFields ++
      models.graphql.note.NoteRowSchema.queryFields ++
      /* End model query fields */
      Nil
  }

  private[this] val enumQueryFields: Seq[Field[GraphQLContext, Unit]] = {
    Nil ++
      /* Start enum query fields */
      /* End enum query fields */
      Nil
  }

  private[this] val serviceQueryFields: Seq[Field[GraphQLContext, Unit]] = {
    Nil ++
      /* Start service methods */
      /* End service methods */
      Nil
  }

  override val queryType = ObjectType(
    name = "Query",
    description = "The main query interface.",
    fields = (baseQueryFields ++ modelQueryFields ++ enumQueryFields ++ serviceQueryFields).sortBy(_.name)
  )

  // Mutation Types
  private[this] val baseMutationFields = Nil

  private[this] val modelMutationFields: Seq[Field[GraphQLContext, Unit]] = {
    Nil ++
      /* Start model mutation fields */
      /* Projectile export section [websocket] */
      models.graphql.auth.Oauth2InfoRowSchema.mutationFields ++
      models.graphql.auth.PasswordInfoRowSchema.mutationFields ++
      models.graphql.auth.SystemUserRowSchema.mutationFields ++
      models.graphql.note.NoteRowSchema.mutationFields ++
      /* End model mutation fields */
      Nil
  }

  override val mutationType = ObjectType(
    name = "Mutation",
    description = "The main mutation interface.",
    fields = (baseMutationFields ++ modelMutationFields).sortBy(_.name)
  )
}
