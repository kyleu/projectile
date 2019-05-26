package com.kyleu.projectile.models.graphql.task

import com.kyleu.projectile.graphql.{GraphQLContext, GraphQLSchemaHelper}
import com.kyleu.projectile.graphql.GraphQLUtils._
import java.util.UUID

import com.kyleu.projectile.models.task.{ScheduledTaskRun, ScheduledTaskRunResult}
import com.kyleu.projectile.services.task.ScheduledTaskRunService
import sangria.execution.deferred.{Fetcher, HasId}
import sangria.schema._

import scala.concurrent.Future

object ScheduledTaskRunSchema extends GraphQLSchemaHelper("scheduledTaskRun") {
  implicit val scheduledTaskRunPrimaryKeyId: HasId[ScheduledTaskRun, UUID] = HasId[ScheduledTaskRun, UUID](_.id)
  private[this] def getByPrimaryKeySeq(c: GraphQLContext, idSeq: Seq[UUID]) = {
    c.getInstance[ScheduledTaskRunService].getByPrimaryKeySeq(c.creds, idSeq)(c.trace)
  }
  val scheduledTaskRunByPrimaryKeyFetcher = Fetcher(getByPrimaryKeySeq)

  val scheduledTaskRunIdArg = Argument("id", uuidType)
  val scheduledTaskRunIdSeqArg = Argument("ids", ListInputType(uuidType))

  val scheduledTaskRunTaskArg = Argument("task", StringType)
  val scheduledTaskRunTaskSeqArg = Argument("tasks", ListInputType(StringType))
  val scheduledTaskRunArgumentsArg = Argument("arguments", ListInputType(StringType))
  val scheduledTaskRunArgumentsSeqArg = Argument("argumentss", ListInputType(ListInputType(StringType)))
  val scheduledTaskRunStatusArg = Argument("status", StringType)
  val scheduledTaskRunStatusSeqArg = Argument("statuss", ListInputType(StringType))
  val scheduledTaskRunStartedArg = Argument("started", localDateTimeType)
  val scheduledTaskRunStartedSeqArg = Argument("starteds", ListInputType(localDateTimeType))

  implicit lazy val scheduledTaskRunType: sangria.schema.ObjectType[GraphQLContext, ScheduledTaskRun] = deriveObjectType()

  implicit lazy val scheduledTaskRunResultType: sangria.schema.ObjectType[GraphQLContext, ScheduledTaskRunResult] = deriveObjectType()

  val queryFields = fields(
    unitField(name = "scheduledTaskRun", desc = None, t = OptionType(scheduledTaskRunType), f = (c, td) => {
      c.ctx.getInstance[ScheduledTaskRunService].getByPrimaryKey(c.ctx.creds, c.arg(scheduledTaskRunIdArg))(td)
    }, scheduledTaskRunIdArg),
    unitField(name = "scheduledTaskRunSeq", desc = None, t = ListType(scheduledTaskRunType), f = (c, td) => {
      c.ctx.getInstance[ScheduledTaskRunService].getByPrimaryKeySeq(c.ctx.creds, c.arg(scheduledTaskRunIdSeqArg))(td)
    }, scheduledTaskRunIdSeqArg),
    unitField(name = "scheduledTaskRunSearch", desc = None, t = scheduledTaskRunResultType, f = (c, td) => {
      runSearch(c.ctx.getInstance[ScheduledTaskRunService], c, td).map(toResult)
    }, queryArg, reportFiltersArg, orderBysArg, limitArg, offsetArg),
    unitField(name = "scheduledTaskRunsByTask", desc = None, t = ListType(scheduledTaskRunType), f = (c, td) => {
      c.ctx.getInstance[ScheduledTaskRunService].getByTask(c.ctx.creds, c.arg(scheduledTaskRunTaskArg))(td)
    }, scheduledTaskRunTaskArg),
    unitField(name = "scheduledTaskRunsByTaskSeq", desc = None, t = ListType(scheduledTaskRunType), f = (c, td) => {
      c.ctx.getInstance[ScheduledTaskRunService].getByTaskSeq(c.ctx.creds, c.arg(scheduledTaskRunTaskSeqArg))(td)
    }, scheduledTaskRunTaskSeqArg),
    unitField(name = "scheduledTaskRunsByArguments", desc = None, t = ListType(scheduledTaskRunType), f = (c, td) => {
      c.ctx.getInstance[ScheduledTaskRunService].getByArguments(c.ctx.creds, c.arg(scheduledTaskRunArgumentsArg).toList)(td)
    }, scheduledTaskRunArgumentsArg),
    unitField(name = "scheduledTaskRunsByArgumentsSeq", desc = None, t = ListType(scheduledTaskRunType), f = (c, td) => {
      c.ctx.getInstance[ScheduledTaskRunService].getByArgumentsSeq(c.ctx.creds, c.arg(scheduledTaskRunArgumentsSeqArg).map(_.toList))(td)
    }, scheduledTaskRunArgumentsSeqArg),
    unitField(name = "scheduledTaskRunsByStatus", desc = None, t = ListType(scheduledTaskRunType), f = (c, td) => {
      c.ctx.getInstance[ScheduledTaskRunService].getByStatus(c.ctx.creds, c.arg(scheduledTaskRunStatusArg))(td)
    }, scheduledTaskRunStatusArg),
    unitField(name = "scheduledTaskRunsByStatusSeq", desc = None, t = ListType(scheduledTaskRunType), f = (c, td) => {
      c.ctx.getInstance[ScheduledTaskRunService].getByStatusSeq(c.ctx.creds, c.arg(scheduledTaskRunStatusSeqArg))(td)
    }, scheduledTaskRunStatusSeqArg),
    unitField(name = "scheduledTaskRunsByStarted", desc = None, t = ListType(scheduledTaskRunType), f = (c, td) => {
      c.ctx.getInstance[ScheduledTaskRunService].getByStarted(c.ctx.creds, c.arg(scheduledTaskRunStartedArg))(td)
    }, scheduledTaskRunStartedArg),
    unitField(name = "scheduledTaskRunsByStartedSeq", desc = None, t = ListType(scheduledTaskRunType), f = (c, td) => {
      c.ctx.getInstance[ScheduledTaskRunService].getByStartedSeq(c.ctx.creds, c.arg(scheduledTaskRunStartedSeqArg))(td)
    }, scheduledTaskRunStartedSeqArg)
  )

  val scheduledTaskRunMutationType = ObjectType(
    name = "ScheduledTaskRunMutations",
    fields = fields(
      unitField(name = "create", desc = None, t = OptionType(scheduledTaskRunType), f = (c, td) => {
        c.ctx.getInstance[ScheduledTaskRunService].create(c.ctx.creds, c.arg(dataFieldsArg))(td)
      }, dataFieldsArg),
      unitField(name = "update", desc = None, t = OptionType(scheduledTaskRunType), f = (c, td) => {
        c.ctx.getInstance[ScheduledTaskRunService].update(c.ctx.creds, c.arg(scheduledTaskRunIdArg), c.arg(dataFieldsArg))(td).map(_._1)
      }, scheduledTaskRunIdArg, dataFieldsArg),
      unitField(name = "remove", desc = None, t = scheduledTaskRunType, f = (c, td) => {
        c.ctx.getInstance[ScheduledTaskRunService].remove(c.ctx.creds, c.arg(scheduledTaskRunIdArg))(td)
      }, scheduledTaskRunIdArg)
    )
  )

  val mutationFields = fields(unitField(name = "scheduledTaskRun", desc = None, t = scheduledTaskRunMutationType, f = (_, _) => Future.successful(())))

  private[this] def toResult(r: GraphQLSchemaHelper.SearchResult[ScheduledTaskRun]) = {
    ScheduledTaskRunResult(r.args.filters, r.args.orderBys, r.count, r.paging, r.results, r.dur)
  }
}
