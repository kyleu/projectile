package com.kyleu.projectile.models.graphql.error

import com.kyleu.projectile.graphql.{GraphQLContext, GraphQLSchemaHelper}
import com.kyleu.projectile.graphql.GraphQLUtils._
import java.util.UUID

import com.kyleu.projectile.models.error.{SystemError, SystemErrorResult}
import com.kyleu.projectile.services.error.SystemErrorService
import sangria.execution.deferred.{Fetcher, HasId}
import sangria.schema._

object SystemErrorSchema extends GraphQLSchemaHelper("systemError") {
  implicit val systemErrorPrimaryKeyId: HasId[SystemError, UUID] = HasId[SystemError, UUID](_.id)
  private[this] def getByPrimaryKeySeq(c: GraphQLContext, idSeq: Seq[UUID]) = c.getInstance[SystemErrorService].getByPrimaryKeySeq(c.creds, idSeq)(c.trace)
  val systemErrorByPrimaryKeyFetcher = Fetcher(getByPrimaryKeySeq)

  val systemErrorIdArg = Argument("id", uuidType)
  val systemErrorIdSeqArg = Argument("ids", ListInputType(uuidType))

  val systemErrorContextArg = Argument("context", StringType)
  val systemErrorContextSeqArg = Argument("contexts", ListInputType(StringType))
  val systemErrorUserIdArg = Argument("userId", uuidType)
  val systemErrorUserIdSeqArg = Argument("userIds", ListInputType(uuidType))
  val systemErrorClsArg = Argument("cls", StringType)
  val systemErrorClsSeqArg = Argument("clss", ListInputType(StringType))
  val systemErrorMessageArg = Argument("message", StringType)
  val systemErrorMessageSeqArg = Argument("messages", ListInputType(StringType))

  implicit lazy val systemErrorType: sangria.schema.ObjectType[GraphQLContext, SystemError] = deriveObjectType()

  implicit lazy val systemErrorResultType: sangria.schema.ObjectType[GraphQLContext, SystemErrorResult] = deriveObjectType()

  val queryFields = fields(
    unitField(name = "systemError", desc = None, t = OptionType(systemErrorType), f = (c, td) => {
      c.ctx.getInstance[SystemErrorService].getByPrimaryKey(c.ctx.creds, c.arg(systemErrorIdArg))(td)
    }, systemErrorIdArg),
    unitField(name = "systemErrorSeq", desc = None, t = ListType(systemErrorType), f = (c, td) => {
      c.ctx.getInstance[SystemErrorService].getByPrimaryKeySeq(c.ctx.creds, c.arg(systemErrorIdSeqArg))(td)
    }, systemErrorIdSeqArg),
    unitField(name = "systemErrorSearch", desc = None, t = systemErrorResultType, f = (c, td) => {
      runSearch(c.ctx.getInstance[SystemErrorService], c, td).map(toResult)
    }, queryArg, reportFiltersArg, orderBysArg, limitArg, offsetArg),
    unitField(name = "systemErrorsByContext", desc = None, t = ListType(systemErrorType), f = (c, td) => {
      c.ctx.getInstance[SystemErrorService].getByContext(c.ctx.creds, c.arg(systemErrorContextArg))(td)
    }, systemErrorContextArg),
    unitField(name = "systemErrorsByContextSeq", desc = None, t = ListType(systemErrorType), f = (c, td) => {
      c.ctx.getInstance[SystemErrorService].getByContextSeq(c.ctx.creds, c.arg(systemErrorContextSeqArg))(td)
    }, systemErrorContextSeqArg),
    unitField(name = "systemErrorsByUserId", desc = None, t = ListType(systemErrorType), f = (c, td) => {
      c.ctx.getInstance[SystemErrorService].getByUserId(c.ctx.creds, c.arg(systemErrorUserIdArg))(td)
    }, systemErrorUserIdArg),
    unitField(name = "systemErrorsByUserIdSeq", desc = None, t = ListType(systemErrorType), f = (c, td) => {
      c.ctx.getInstance[SystemErrorService].getByUserIdSeq(c.ctx.creds, c.arg(systemErrorUserIdSeqArg))(td)
    }, systemErrorUserIdSeqArg),
    unitField(name = "systemErrorsByCls", desc = None, t = ListType(systemErrorType), f = (c, td) => {
      c.ctx.getInstance[SystemErrorService].getByCls(c.ctx.creds, c.arg(systemErrorClsArg))(td)
    }, systemErrorClsArg),
    unitField(name = "systemErrorsByClsSeq", desc = None, t = ListType(systemErrorType), f = (c, td) => {
      c.ctx.getInstance[SystemErrorService].getByClsSeq(c.ctx.creds, c.arg(systemErrorClsSeqArg))(td)
    }, systemErrorClsSeqArg),
    unitField(name = "systemErrorsByMessage", desc = None, t = ListType(systemErrorType), f = (c, td) => {
      c.ctx.getInstance[SystemErrorService].getByMessage(c.ctx.creds, c.arg(systemErrorMessageArg))(td)
    }, systemErrorMessageArg),
    unitField(name = "systemErrorsByMessageSeq", desc = None, t = ListType(systemErrorType), f = (c, td) => {
      c.ctx.getInstance[SystemErrorService].getByMessageSeq(c.ctx.creds, c.arg(systemErrorMessageSeqArg))(td)
    }, systemErrorMessageSeqArg)
  )

  val systemErrorMutationType = ObjectType(
    name = "SystemErrorMutations",
    fields = fields(
      unitField(name = "create", desc = None, t = OptionType(systemErrorType), f = (c, td) => {
        c.ctx.getInstance[SystemErrorService].create(c.ctx.creds, c.arg(dataFieldsArg))(td)
      }, dataFieldsArg),
      unitField(name = "update", desc = None, t = OptionType(systemErrorType), f = (c, td) => {
        c.ctx.getInstance[SystemErrorService].update(c.ctx.creds, c.arg(systemErrorIdArg), c.arg(dataFieldsArg))(td).map(_._1)
      }, systemErrorIdArg, dataFieldsArg),
      unitField(name = "remove", desc = None, t = systemErrorType, f = (c, td) => {
        c.ctx.getInstance[SystemErrorService].remove(c.ctx.creds, c.arg(systemErrorIdArg))(td)
      }, systemErrorIdArg)
    )
  )

  val mutationFields = fields(unitField(name = "systemError", desc = None, t = systemErrorMutationType, f = (_, _) => scala.concurrent.Future.successful(())))

  private[this] def toResult(r: GraphQLSchemaHelper.SearchResult[SystemError]) = {
    SystemErrorResult(paging = r.paging, filters = r.args.filters, orderBys = r.args.orderBys, totalCount = r.count, results = r.results, durationMs = r.dur)
  }
}
