// scalastyle:off file.size.limit
package com.kyleu.projectile.models.graphql.feedback

import com.kyleu.projectile.graphql.{GraphQLContext, GraphQLSchemaHelper}
import com.kyleu.projectile.graphql.GraphQLUtils._
import java.util.UUID

import com.kyleu.projectile.models.feedback.{Feedback, FeedbackResult}
import com.kyleu.projectile.models.graphql.user.SystemUserSchema
import com.kyleu.projectile.services.feedback.FeedbackService
import sangria.execution.deferred.{Fetcher, HasId, Relation}
import sangria.schema._

object FeedbackSchema extends GraphQLSchemaHelper("feedback") {
  implicit val feedbackPrimaryKeyId: HasId[Feedback, UUID] = HasId[Feedback, UUID](_.id)
  private[this] def getByPrimaryKeySeq(c: GraphQLContext, idSeq: Seq[UUID]) = c.getInstance[FeedbackService].getByPrimaryKeySeq(c.creds, idSeq)(c.trace)
  val feedbackByPrimaryKeyFetcher = Fetcher(getByPrimaryKeySeq)

  val feedbackIdArg = Argument("id", uuidType)
  val feedbackIdSeqArg = Argument("ids", ListInputType(uuidType))

  val feedbackTextArg = Argument("text", StringType)
  val feedbackTextSeqArg = Argument("texts", ListInputType(StringType))
  val feedbackAuthorIdArg = Argument("authorId", uuidType)
  val feedbackAuthorIdSeqArg = Argument("authorIds", ListInputType(uuidType))
  val feedbackAuthorEmailArg = Argument("authorEmail", StringType)
  val feedbackAuthorEmailSeqArg = Argument("authorEmails", ListInputType(StringType))
  val feedbackCreatedArg = Argument("created", localDateTimeType)
  val feedbackCreatedSeqArg = Argument("createds", ListInputType(localDateTimeType))
  val feedbackStatusArg = Argument("status", StringType)
  val feedbackStatusSeqArg = Argument("statuss", ListInputType(StringType))

  val feedbackByAuthorIdRelation = Relation[Feedback, UUID]("byAuthorId", x => Seq(x.authorId))
  val feedbackByAuthorIdFetcher = Fetcher.rel[GraphQLContext, Feedback, Feedback, UUID](
    getByPrimaryKeySeq, (c, rels) => c.injector.getInstance(classOf[FeedbackService]).getByAuthorIdSeq(c.creds, rels(feedbackByAuthorIdRelation))(c.trace)
  )

  implicit lazy val feedbackType: sangria.schema.ObjectType[GraphQLContext, Feedback] = deriveObjectType(
    sangria.macros.derive.AddFields(
      Field(
        name = "author",
        fieldType = SystemUserSchema.systemUserType,
        resolve = ctx => SystemUserSchema.systemUserByPrimaryKeyFetcher.defer(ctx.value.authorId)
      )
    )
  )

  implicit lazy val feedbackResultType: sangria.schema.ObjectType[GraphQLContext, FeedbackResult] = deriveObjectType()

  val queryFields = fields(
    unitField(name = "feedback", desc = None, t = OptionType(feedbackType), f = (c, td) => {
      c.ctx.getInstance[FeedbackService].getByPrimaryKey(c.ctx.creds, c.arg(feedbackIdArg))(td)
    }, feedbackIdArg),
    unitField(name = "feedbackSeq", desc = None, t = ListType(feedbackType), f = (c, td) => {
      c.ctx.getInstance[FeedbackService].getByPrimaryKeySeq(c.ctx.creds, c.arg(feedbackIdSeqArg))(td)
    }, feedbackIdSeqArg),
    unitField(name = "feedbackSearch", desc = None, t = feedbackResultType, f = (c, td) => {
      runSearch(c.ctx.getInstance[FeedbackService], c, td).map(toResult)
    }, queryArg, reportFiltersArg, orderBysArg, limitArg, offsetArg),
    unitField(name = "feedbacksByText", desc = None, t = ListType(feedbackType), f = (c, td) => {
      c.ctx.getInstance[FeedbackService].getByText(c.ctx.creds, c.arg(feedbackTextArg))(td)
    }, feedbackTextArg),
    unitField(name = "feedbacksByTextSeq", desc = None, t = ListType(feedbackType), f = (c, td) => {
      c.ctx.getInstance[FeedbackService].getByTextSeq(c.ctx.creds, c.arg(feedbackTextSeqArg))(td)
    }, feedbackTextSeqArg),
    unitField(name = "feedbacksByAuthorId", desc = None, t = ListType(feedbackType), f = (c, td) => {
      c.ctx.getInstance[FeedbackService].getByAuthorId(c.ctx.creds, c.arg(feedbackAuthorIdArg))(td)
    }, feedbackAuthorIdArg),
    unitField(name = "feedbacksByAuthorIdSeq", desc = None, t = ListType(feedbackType), f = (c, td) => {
      c.ctx.getInstance[FeedbackService].getByAuthorIdSeq(c.ctx.creds, c.arg(feedbackAuthorIdSeqArg))(td)
    }, feedbackAuthorIdSeqArg),
    unitField(name = "feedbacksByAuthorEmail", desc = None, t = ListType(feedbackType), f = (c, td) => {
      c.ctx.getInstance[FeedbackService].getByAuthorEmail(c.ctx.creds, c.arg(feedbackAuthorEmailArg))(td)
    }, feedbackAuthorEmailArg),
    unitField(name = "feedbacksByAuthorEmailSeq", desc = None, t = ListType(feedbackType), f = (c, td) => {
      c.ctx.getInstance[FeedbackService].getByAuthorEmailSeq(c.ctx.creds, c.arg(feedbackAuthorEmailSeqArg))(td)
    }, feedbackAuthorEmailSeqArg),
    unitField(name = "feedbacksByCreated", desc = None, t = ListType(feedbackType), f = (c, td) => {
      c.ctx.getInstance[FeedbackService].getByCreated(c.ctx.creds, c.arg(feedbackCreatedArg))(td)
    }, feedbackCreatedArg),
    unitField(name = "feedbacksByCreatedSeq", desc = None, t = ListType(feedbackType), f = (c, td) => {
      c.ctx.getInstance[FeedbackService].getByCreatedSeq(c.ctx.creds, c.arg(feedbackCreatedSeqArg))(td)
    }, feedbackCreatedSeqArg),
    unitField(name = "feedbacksByStatus", desc = None, t = ListType(feedbackType), f = (c, td) => {
      c.ctx.getInstance[FeedbackService].getByStatus(c.ctx.creds, c.arg(feedbackStatusArg))(td)
    }, feedbackStatusArg),
    unitField(name = "feedbacksByStatusSeq", desc = None, t = ListType(feedbackType), f = (c, td) => {
      c.ctx.getInstance[FeedbackService].getByStatusSeq(c.ctx.creds, c.arg(feedbackStatusSeqArg))(td)
    }, feedbackStatusSeqArg)
  )

  val feedbackMutationType = ObjectType(
    name = "FeedbackMutations",
    fields = fields(
      unitField(name = "create", desc = None, t = OptionType(feedbackType), f = (c, td) => {
        c.ctx.getInstance[FeedbackService].create(c.ctx.creds, c.arg(dataFieldsArg))(td)
      }, dataFieldsArg),
      unitField(name = "update", desc = None, t = OptionType(feedbackType), f = (c, td) => {
        c.ctx.getInstance[FeedbackService].update(c.ctx.creds, c.arg(feedbackIdArg), c.arg(dataFieldsArg))(td).map(_._1)
      }, feedbackIdArg, dataFieldsArg),
      unitField(name = "remove", desc = None, t = feedbackType, f = (c, td) => {
        c.ctx.getInstance[FeedbackService].remove(c.ctx.creds, c.arg(feedbackIdArg))(td)
      }, feedbackIdArg)
    )
  )

  val mutationFields = fields(unitField(name = "feedback", desc = None, t = feedbackMutationType, f = (_, _) => scala.concurrent.Future.successful(())))

  private[this] def toResult(r: GraphQLSchemaHelper.SearchResult[Feedback]) = {
    FeedbackResult(paging = r.paging, filters = r.args.filters, orderBys = r.args.orderBys, totalCount = r.count, results = r.results, durationMs = r.dur)
  }
}
