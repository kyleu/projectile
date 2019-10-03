/* Generated File */
package models.graphql.size

import com.kyleu.projectile.graphql.{GraphQLContext, GraphQLSchemaHelper}
import com.kyleu.projectile.graphql.GraphQLUtils._
import models.size.{SmallRow, SmallRowResult}
import sangria.execution.deferred.{Fetcher, HasId, Relation}
import sangria.schema._

object SmallRowSchema extends GraphQLSchemaHelper("smallRow") {
  implicit val smallRowPrimaryKeyId: HasId[SmallRow, Long] = HasId[SmallRow, Long](_.id)
  private[this] def getByPrimaryKeySeq(c: GraphQLContext, idSeq: Seq[Long]) = c.getInstance[services.size.SmallRowService].getByPrimaryKeySeq(c.creds, idSeq)(c.trace)
  val smallRowByPrimaryKeyFetcher = Fetcher(getByPrimaryKeySeq)

  val smallRowIdArg = Argument("id", LongType)
  val smallRowIdSeqArg = Argument("ids", ListInputType(LongType))

  val smallRowBigIdArg = Argument("bigId", LongType)
  val smallRowBigIdSeqArg = Argument("bigIds", ListInputType(LongType))
  val smallRowTArg = Argument("t", StringType)
  val smallRowTSeqArg = Argument("ts", ListInputType(StringType))

  val smallRowByBigIdRelation = Relation[SmallRow, Long]("byBigId", x => Seq(x.bigId))
  val smallRowByBigIdFetcher = Fetcher.rel[GraphQLContext, SmallRow, SmallRow, Long](
    getByPrimaryKeySeq, (c, rels) => c.injector.getInstance(classOf[services.size.SmallRowService]).getByBigIdSeq(c.creds, rels(smallRowByBigIdRelation))(c.trace)
  )

  implicit lazy val smallRowType: sangria.schema.ObjectType[GraphQLContext, SmallRow] = deriveObjectType(
    sangria.macros.derive.AddFields(
      Field(
        name = "big",
        fieldType = BigRowSchema.bigRowType,
        resolve = ctx => BigRowSchema.bigRowByPrimaryKeyFetcher.defer(ctx.value.bigId)
      )
    )
  )

  implicit lazy val smallRowResultType: sangria.schema.ObjectType[GraphQLContext, SmallRowResult] = deriveObjectType()

  val queryFields = fields(
    unitField(name = "smallRow", desc = None, t = OptionType(smallRowType), f = (c, td) => {
      c.ctx.getInstance[services.size.SmallRowService].getByPrimaryKey(c.ctx.creds, c.arg(smallRowIdArg))(td)
    }, smallRowIdArg),
    unitField(name = "smallRowSeq", desc = None, t = ListType(smallRowType), f = (c, td) => {
      c.ctx.getInstance[services.size.SmallRowService].getByPrimaryKeySeq(c.ctx.creds, c.arg(smallRowIdSeqArg))(td)
    }, smallRowIdSeqArg),
    unitField(name = "smallRowSearch", desc = None, t = smallRowResultType, f = (c, td) => {
      runSearch(c.ctx.getInstance[services.size.SmallRowService], c, td).map(toResult)
    }, queryArg, reportFiltersArg, orderBysArg, limitArg, offsetArg),
    unitField(name = "smallsByBigId", desc = None, t = ListType(smallRowType), f = (c, td) => {
      c.ctx.getInstance[services.size.SmallRowService].getByBigId(c.ctx.creds, c.arg(smallRowBigIdArg))(td)
    }, smallRowBigIdArg),
    unitField(name = "smallsByBigIdSeq", desc = None, t = ListType(smallRowType), f = (c, td) => {
      c.ctx.getInstance[services.size.SmallRowService].getByBigIdSeq(c.ctx.creds, c.arg(smallRowBigIdSeqArg))(td)
    }, smallRowBigIdSeqArg),
    unitField(name = "smallsByT", desc = None, t = ListType(smallRowType), f = (c, td) => {
      c.ctx.getInstance[services.size.SmallRowService].getByT(c.ctx.creds, c.arg(smallRowTArg))(td)
    }, smallRowTArg),
    unitField(name = "smallsByTSeq", desc = None, t = ListType(smallRowType), f = (c, td) => {
      c.ctx.getInstance[services.size.SmallRowService].getByTSeq(c.ctx.creds, c.arg(smallRowTSeqArg))(td)
    }, smallRowTSeqArg)
  )

  val smallRowMutationType = ObjectType(
    name = "SmallRowMutations",
    fields = fields(
      unitField(name = "create", desc = None, t = OptionType(smallRowType), f = (c, td) => {
        c.ctx.getInstance[services.size.SmallRowService].create(c.ctx.creds, c.arg(dataFieldsArg))(td)
      }, dataFieldsArg),
      unitField(name = "update", desc = None, t = OptionType(smallRowType), f = (c, td) => {
        c.ctx.getInstance[services.size.SmallRowService].update(c.ctx.creds, c.arg(smallRowIdArg), c.arg(dataFieldsArg))(td).map(_._1)
      }, smallRowIdArg, dataFieldsArg),
      unitField(name = "remove", desc = None, t = smallRowType, f = (c, td) => {
        c.ctx.getInstance[services.size.SmallRowService].remove(c.ctx.creds, c.arg(smallRowIdArg))(td)
      }, smallRowIdArg)
    )
  )

  val mutationFields = fields(unitField(name = "smallRow", desc = None, t = smallRowMutationType, f = (_, _) => scala.concurrent.Future.successful(())))

  private[this] def toResult(r: GraphQLSchemaHelper.SearchResult[SmallRow]) = {
    SmallRowResult(paging = r.paging, filters = r.args.filters, orderBys = r.args.orderBys, totalCount = r.count, results = r.results, durationMs = r.dur)
  }
}
