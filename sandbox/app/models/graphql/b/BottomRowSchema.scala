/* Generated File */
package models.graphql.b

import com.kyleu.projectile.graphql.{GraphQLContext, GraphQLSchemaHelper}
import com.kyleu.projectile.graphql.GraphQLUtils._
import java.util.UUID
import models.b.{BottomRow, BottomRowResult}
import models.graphql.t.TopRowSchema
import sangria.execution.deferred.{Fetcher, HasId, Relation}
import sangria.schema._

object BottomRowSchema extends GraphQLSchemaHelper("bottomRow") {
  implicit val bottomRowPrimaryKeyId: HasId[BottomRow, UUID] = HasId[BottomRow, UUID](_.id)
  private[this] def getByPrimaryKeySeq(c: GraphQLContext, idSeq: Seq[UUID]) = c.getInstance[services.b.BottomRowService].getByPrimaryKeySeq(c.creds, idSeq)(c.trace)
  val bottomRowByPrimaryKeyFetcher = Fetcher(getByPrimaryKeySeq)

  val bottomRowIdArg = Argument("id", uuidType)
  val bottomRowIdSeqArg = Argument("ids", ListInputType(uuidType))

  val bottomRowTopIdArg = Argument("topId", uuidType)
  val bottomRowTopIdSeqArg = Argument("topIds", ListInputType(uuidType))
  val bottomRowTArg = Argument("t", StringType)
  val bottomRowTSeqArg = Argument("ts", ListInputType(StringType))

  val bottomRowByTopIdRelation = Relation[BottomRow, UUID]("byTopId", x => Seq(x.topId))
  val bottomRowByTopIdFetcher = Fetcher.rel[GraphQLContext, BottomRow, BottomRow, UUID](
    getByPrimaryKeySeq, (c, rels) => c.injector.getInstance(classOf[services.b.BottomRowService]).getByTopIdSeq(c.creds, rels(bottomRowByTopIdRelation))(c.trace)
  )

  implicit lazy val bottomRowType: sangria.schema.ObjectType[GraphQLContext, BottomRow] = deriveObjectType(
    sangria.macros.derive.AddFields(
      Field(
        name = "bottomTopIdFkeyRel",
        fieldType = TopRowSchema.topRowType,
        resolve = ctx => TopRowSchema.topRowByPrimaryKeyFetcher.defer(ctx.value.topId)
      )
    )
  )

  implicit lazy val bottomRowResultType: sangria.schema.ObjectType[GraphQLContext, BottomRowResult] = deriveObjectType()

  val queryFields = fields(
    unitField(name = "bottomRow", desc = None, t = OptionType(bottomRowType), f = (c, td) => {
      c.ctx.getInstance[services.b.BottomRowService].getByPrimaryKey(c.ctx.creds, c.arg(bottomRowIdArg))(td)
    }, bottomRowIdArg),
    unitField(name = "bottomRowSeq", desc = None, t = ListType(bottomRowType), f = (c, td) => {
      c.ctx.getInstance[services.b.BottomRowService].getByPrimaryKeySeq(c.ctx.creds, c.arg(bottomRowIdSeqArg))(td)
    }, bottomRowIdSeqArg),
    unitField(name = "bottomRowSearch", desc = None, t = bottomRowResultType, f = (c, td) => {
      runSearch(c.ctx.getInstance[services.b.BottomRowService], c, td).map(toResult)
    }, queryArg, reportFiltersArg, orderBysArg, limitArg, offsetArg),
    unitField(name = "bottomsByTopId", desc = None, t = ListType(bottomRowType), f = (c, td) => {
      c.ctx.getInstance[services.b.BottomRowService].getByTopId(c.ctx.creds, c.arg(bottomRowTopIdArg))(td)
    }, bottomRowTopIdArg),
    unitField(name = "bottomsByTopIdSeq", desc = None, t = ListType(bottomRowType), f = (c, td) => {
      c.ctx.getInstance[services.b.BottomRowService].getByTopIdSeq(c.ctx.creds, c.arg(bottomRowTopIdSeqArg))(td)
    }, bottomRowTopIdSeqArg),
    unitField(name = "bottomsByT", desc = None, t = ListType(bottomRowType), f = (c, td) => {
      c.ctx.getInstance[services.b.BottomRowService].getByT(c.ctx.creds, c.arg(bottomRowTArg))(td)
    }, bottomRowTArg),
    unitField(name = "bottomsByTSeq", desc = None, t = ListType(bottomRowType), f = (c, td) => {
      c.ctx.getInstance[services.b.BottomRowService].getByTSeq(c.ctx.creds, c.arg(bottomRowTSeqArg))(td)
    }, bottomRowTSeqArg)
  )

  val bottomRowMutationType = ObjectType(
    name = "BottomRowMutations",
    fields = fields(
      unitField(name = "create", desc = None, t = OptionType(bottomRowType), f = (c, td) => {
        c.ctx.getInstance[services.b.BottomRowService].create(c.ctx.creds, c.arg(dataFieldsArg))(td)
      }, dataFieldsArg),
      unitField(name = "update", desc = None, t = OptionType(bottomRowType), f = (c, td) => {
        c.ctx.getInstance[services.b.BottomRowService].update(c.ctx.creds, c.arg(bottomRowIdArg), c.arg(dataFieldsArg))(td).map(_._1)
      }, bottomRowIdArg, dataFieldsArg),
      unitField(name = "remove", desc = None, t = bottomRowType, f = (c, td) => {
        c.ctx.getInstance[services.b.BottomRowService].remove(c.ctx.creds, c.arg(bottomRowIdArg))(td)
      }, bottomRowIdArg)
    )
  )

  val mutationFields = fields(unitField(name = "bottomRow", desc = None, t = bottomRowMutationType, f = (_, _) => scala.concurrent.Future.successful(())))

  private[this] def toResult(r: GraphQLSchemaHelper.SearchResult[BottomRow]) = {
    BottomRowResult(paging = r.paging, filters = r.args.filters, orderBys = r.args.orderBys, totalCount = r.count, results = r.results, durationMs = r.dur)
  }
}
