// scalastyle:off file.size.limit
package com.kyleu.projectile.models.graphql.audit

import com.kyleu.projectile.graphql.{GraphQLContext, GraphQLSchemaHelper}
import com.kyleu.projectile.graphql.GraphQLUtils._
import java.util.UUID

import com.kyleu.projectile.models.audit.{Audit, AuditResult}
import com.kyleu.projectile.services.audit.AuditService
import sangria.execution.deferred.{Fetcher, HasId}
import sangria.schema._

object AuditSchema extends GraphQLSchemaHelper("audit") {
  implicit val auditPrimaryKeyId: HasId[Audit, UUID] = HasId[Audit, UUID](_.id)
  private[this] def getByPrimaryKeySeq(c: GraphQLContext, idSeq: Seq[UUID]) = {
    c.getInstance[AuditService].getByPrimaryKeySeq(c.creds, idSeq)(c.trace)
  }
  val auditByPrimaryKeyFetcher = Fetcher(getByPrimaryKeySeq)

  val auditIdArg = Argument("id", uuidType)
  val auditIdSeqArg = Argument("ids", ListInputType(uuidType))

  val auditActArg = Argument("act", StringType)
  val auditActSeqArg = Argument("acts", ListInputType(StringType))
  val auditAppArg = Argument("app", StringType)
  val auditAppSeqArg = Argument("apps", ListInputType(StringType))
  val auditClientArg = Argument("client", StringType)
  val auditClientSeqArg = Argument("clients", ListInputType(StringType))
  val auditServerArg = Argument("server", StringType)
  val auditServerSeqArg = Argument("servers", ListInputType(StringType))
  val auditUserIdArg = Argument("userId", uuidType)
  val auditUserIdSeqArg = Argument("userIds", ListInputType(uuidType))

  implicit lazy val auditType: sangria.schema.ObjectType[GraphQLContext, Audit] = deriveObjectType(
    sangria.macros.derive.AddFields(
      Field(
        name = "auditRecordAuditIdFkey",
        fieldType = ListType(AuditRecordSchema.auditRecordType),
        resolve = c => AuditRecordSchema.auditRecordByAuditIdFetcher.deferRelSeq(
          AuditRecordSchema.auditRecordByAuditIdRelation, c.value.id
        )
      )
    )
  )

  implicit lazy val auditResultType: sangria.schema.ObjectType[GraphQLContext, AuditResult] = deriveObjectType()

  val queryFields = fields(
    unitField(name = "audit", desc = None, t = OptionType(auditType), f = (c, td) => {
      c.ctx.getInstance[AuditService].getByPrimaryKey(c.ctx.creds, c.arg(auditIdArg))(td)
    }, auditIdArg),
    unitField(name = "auditSeq", desc = None, t = ListType(auditType), f = (c, td) => {
      c.ctx.getInstance[AuditService].getByPrimaryKeySeq(c.ctx.creds, c.arg(auditIdSeqArg))(td)
    }, auditIdSeqArg),
    unitField(name = "auditSearch", desc = None, t = auditResultType, f = (c, td) => {
      runSearch(c.ctx.getInstance[AuditService], c, td).map(toResult)
    }, queryArg, reportFiltersArg, orderBysArg, limitArg, offsetArg),
    unitField(name = "auditsByAct", desc = None, t = ListType(auditType), f = (c, td) => {
      c.ctx.getInstance[AuditService].getByAct(c.ctx.creds, c.arg(auditActArg))(td)
    }, auditActArg),
    unitField(name = "auditsByActSeq", desc = None, t = ListType(auditType), f = (c, td) => {
      c.ctx.getInstance[AuditService].getByActSeq(c.ctx.creds, c.arg(auditActSeqArg))(td)
    }, auditActSeqArg),
    unitField(name = "auditsByApp", desc = None, t = ListType(auditType), f = (c, td) => {
      c.ctx.getInstance[AuditService].getByApp(c.ctx.creds, c.arg(auditAppArg))(td)
    }, auditAppArg),
    unitField(name = "auditsByAppSeq", desc = None, t = ListType(auditType), f = (c, td) => {
      c.ctx.getInstance[AuditService].getByAppSeq(c.ctx.creds, c.arg(auditAppSeqArg))(td)
    }, auditAppSeqArg),
    unitField(name = "auditsByClient", desc = None, t = ListType(auditType), f = (c, td) => {
      c.ctx.getInstance[AuditService].getByClient(c.ctx.creds, c.arg(auditClientArg))(td)
    }, auditClientArg),
    unitField(name = "auditsByClientSeq", desc = None, t = ListType(auditType), f = (c, td) => {
      c.ctx.getInstance[AuditService].getByClientSeq(c.ctx.creds, c.arg(auditClientSeqArg))(td)
    }, auditClientSeqArg),
    unitField(name = "auditsByServer", desc = None, t = ListType(auditType), f = (c, td) => {
      c.ctx.getInstance[AuditService].getByServer(c.ctx.creds, c.arg(auditServerArg))(td)
    }, auditServerArg),
    unitField(name = "auditsByServerSeq", desc = None, t = ListType(auditType), f = (c, td) => {
      c.ctx.getInstance[AuditService].getByServerSeq(c.ctx.creds, c.arg(auditServerSeqArg))(td)
    }, auditServerSeqArg),
    unitField(name = "auditsByUserId", desc = None, t = ListType(auditType), f = (c, td) => {
      c.ctx.getInstance[AuditService].getByUserId(c.ctx.creds, c.arg(auditUserIdArg))(td)
    }, auditUserIdArg),
    unitField(name = "auditsByUserIdSeq", desc = None, t = ListType(auditType), f = (c, td) => {
      c.ctx.getInstance[AuditService].getByUserIdSeq(c.ctx.creds, c.arg(auditUserIdSeqArg))(td)
    }, auditUserIdSeqArg)
  )

  val auditMutationType = ObjectType(
    name = "AuditMutations",
    fields = fields(
      unitField(name = "create", desc = None, t = OptionType(auditType), f = (c, td) => {
        c.ctx.getInstance[AuditService].create(c.ctx.creds, c.arg(dataFieldsArg))(td)
      }, dataFieldsArg),
      unitField(name = "update", desc = None, t = OptionType(auditType), f = (c, td) => {
        c.ctx.getInstance[AuditService].update(c.ctx.creds, c.arg(auditIdArg), c.arg(dataFieldsArg))(td).map(_._1)
      }, auditIdArg, dataFieldsArg),
      unitField(name = "remove", desc = None, t = auditType, f = (c, td) => {
        c.ctx.getInstance[AuditService].remove(c.ctx.creds, c.arg(auditIdArg))(td)
      }, auditIdArg)
    )
  )

  val mutationFields = fields(unitField(name = "audit", desc = None, t = auditMutationType, f = (_, _) => scala.concurrent.Future.successful(())))

  private[this] def toResult(r: GraphQLSchemaHelper.SearchResult[Audit]) = {
    AuditResult(paging = r.paging, filters = r.args.filters, orderBys = r.args.orderBys, totalCount = r.count, results = r.results, durationMs = r.dur)
  }
}
