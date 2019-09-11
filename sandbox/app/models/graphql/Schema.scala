package models.graphql

import com.kyleu.projectile.graphql.GraphQLContext
import com.kyleu.projectile.models.graphql.BaseGraphQLSchema
import sangria.schema._
import util.Version

import scala.concurrent.Future

object Schema extends BaseGraphQLSchema {
  override protected def additionalFetchers = Nil ++
    /* Start model fetchers */
    /* Projectile export section [sandbox] */
    Seq(
      models.graphql.BottomRowSchema.bottomRowByPrimaryKeyFetcher,
      models.graphql.BottomRowSchema.bottomRowByTopIdFetcher,
      models.graphql.TopRowSchema.topRowByPrimaryKeyFetcher
    ) ++
      /* End model fetchers */
      Nil

  override protected def additionalQueryFields = fields[GraphQLContext, Unit](
    Field(name = "status", fieldType = StringType, resolve = c => Future.successful("OK")),
    Field(name = "version", fieldType = StringType, resolve = _ => Future.successful(Version.version))
  ) ++
    /* Start query fields */
    /* Projectile export section [sandbox] */
    models.graphql.BottomRowSchema.queryFields ++
    models.graphql.TopRowSchema.queryFields ++
    /* End query fields */
    Nil

  override protected def additionalMutationFields = Nil ++
    /* Start mutation fields */
    /* Projectile export section [sandbox] */
    models.graphql.BottomRowSchema.mutationFields ++
    models.graphql.TopRowSchema.mutationFields ++
    /* End mutation fields */
    Nil
}
