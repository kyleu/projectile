package com.kyleu.projectile.models.graphql.audit

import com.kyleu.projectile.graphql.{GraphQLContext, GraphQLSchemaHelper}
import com.kyleu.projectile.graphql.GraphQLUtils._
import com.kyleu.projectile.models.graphql.note.NoteSchema
import java.util.UUID

import com.kyleu.projectile.models.audit.{AuditField, AuditRecord, AuditRecordResult}
import com.kyleu.projectile.services.audit.AuditRecordService
import sangria.execution.deferred.{Fetcher, HasId, Relation}
import sangria.schema._

object AuditRecordSchema extends GraphQLSchemaHelper("auditRecord") {
  implicit val auditFieldType: sangria.schema.ObjectType[GraphQLContext, AuditField] = deriveObjectType()

  implicit val auditRecordPrimaryKeyId: HasId[AuditRecord, UUID] = HasId[AuditRecord, UUID](_.id)
  private[this] def getByPrimaryKeySeq(c: GraphQLContext, idSeq: Seq[UUID]) = {
    c.injector.getInstance(classOf[AuditRecordService]).getByPrimaryKeySeq(c.creds, idSeq)(c.trace)
  }
  val auditRecordByPrimaryKeyFetcher = Fetcher(getByPrimaryKeySeq)

  val auditRecordIdArg = Argument("id", uuidType)
  val auditRecordIdSeqArg = Argument("ids", ListInputType(uuidType))

  val auditRecordTArg = Argument("t", StringType)
  val auditRecordTSeqArg = Argument("ts", ListInputType(StringType))
  val auditRecordPkArg = Argument("pk", ListInputType(StringType))
  val auditRecordPkSeqArg = Argument("pks", ListInputType(ListInputType(StringType)))

  val auditRecordByAuditIdRelation = Relation[AuditRecord, UUID]("byAuditId", x => Seq(x.auditId))
  val auditRecordByAuditIdFetcher = Fetcher.rel[GraphQLContext, AuditRecord, AuditRecord, UUID](
    getByPrimaryKeySeq, (c, rels) => c.injector.getInstance(classOf[AuditRecordService]).getByAuditIdSeq(c.creds, rels(auditRecordByAuditIdRelation))(c.trace)
  )

  implicit lazy val auditRecordType: sangria.schema.ObjectType[GraphQLContext, AuditRecord] = deriveObjectType(
    sangria.macros.derive.AddFields(
      Field(
        name = "auditRecordAuditIdFkeyRel",
        fieldType = AuditSchema.auditType,
        resolve = ctx => AuditSchema.auditByPrimaryKeyFetcher.defer(ctx.value.auditId)
      )
    )
  )

  implicit lazy val auditRecordResultType: sangria.schema.ObjectType[GraphQLContext, AuditRecordResult] = deriveObjectType()

  val queryFields = fields(
    unitField(name = "auditRecord", desc = None, t = OptionType(auditRecordType), f = (c, td) => {
      c.ctx.injector.getInstance(classOf[AuditRecordService]).getByPrimaryKey(c.ctx.creds, c.arg(auditRecordIdArg))(td)
    }, auditRecordIdArg),
    unitField(name = "auditRecordSeq", desc = None, t = ListType(auditRecordType), f = (c, td) => {
      c.ctx.injector.getInstance(classOf[AuditRecordService]).getByPrimaryKeySeq(c.ctx.creds, c.arg(auditRecordIdSeqArg))(td)
    }, auditRecordIdSeqArg),
    unitField(name = "auditRecordSearch", desc = None, t = auditRecordResultType, f = (c, td) => {
      runSearch(c.ctx.injector.getInstance(classOf[AuditRecordService]), c, td).map(toResult)
    }, queryArg, reportFiltersArg, orderBysArg, limitArg, offsetArg),
    unitField(name = "auditRecordsByT", desc = None, t = ListType(auditRecordType), f = (c, td) => {
      c.ctx.injector.getInstance(classOf[AuditRecordService]).getByT(c.ctx.creds, c.arg(auditRecordTArg))(td)
    }, auditRecordTArg),
    unitField(name = "auditRecordsByTSeq", desc = None, t = ListType(auditRecordType), f = (c, td) => {
      c.ctx.injector.getInstance(classOf[AuditRecordService]).getByTSeq(c.ctx.creds, c.arg(auditRecordTSeqArg))(td)
    }, auditRecordTSeqArg),
    unitField(name = "auditRecordsByPk", desc = None, t = ListType(auditRecordType), f = (c, td) => {
      c.ctx.injector.getInstance(classOf[AuditRecordService]).getByPk(c.ctx.creds, c.arg(auditRecordPkArg).toList)(td)
    }, auditRecordPkArg),
    unitField(name = "auditRecordsByPkSeq", desc = None, t = ListType(auditRecordType), f = (c, td) => {
      c.ctx.injector.getInstance(classOf[AuditRecordService]).getByPkSeq(c.ctx.creds, c.arg(auditRecordPkSeqArg).map(_.toList))(td)
    }, auditRecordPkSeqArg)
  )

  val auditRecordMutationType = ObjectType(
    name = "AuditRecordMutations",
    fields = fields(
      unitField(name = "create", desc = None, t = OptionType(auditRecordType), f = (c, td) => {
        c.ctx.injector.getInstance(classOf[AuditRecordService]).create(c.ctx.creds, c.arg(dataFieldsArg))(td)
      }, dataFieldsArg),
      unitField(name = "update", desc = None, t = OptionType(auditRecordType), f = (c, td) => {
        c.ctx.injector.getInstance(classOf[AuditRecordService]).update(c.ctx.creds, c.arg(auditRecordIdArg), c.arg(dataFieldsArg))(td).map(_._1)
      }, auditRecordIdArg, dataFieldsArg),
      unitField(name = "remove", desc = None, t = auditRecordType, f = (c, td) => {
        c.ctx.injector.getInstance(classOf[AuditRecordService]).remove(c.ctx.creds, c.arg(auditRecordIdArg))(td)
      }, auditRecordIdArg)
    )
  )

  val mutationFields = fields(unitField(name = "auditRecord", desc = None, t = auditRecordMutationType, f = (_, _) => scala.concurrent.Future.successful(())))

  private[this] def toResult(r: GraphQLSchemaHelper.SearchResult[AuditRecord]) = {
    AuditRecordResult(paging = r.paging, filters = r.args.filters, orderBys = r.args.orderBys, totalCount = r.count, results = r.results, durationMs = r.dur)
  }
}
