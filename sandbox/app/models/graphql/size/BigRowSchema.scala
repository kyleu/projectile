/* Generated File */
package models.graphql.size

import com.kyleu.projectile.graphql.{GraphQLContext, GraphQLSchemaHelper}
import com.kyleu.projectile.graphql.GraphQLUtils._
import models.size.{BigRow, BigRowResult}
import sangria.execution.deferred.{Fetcher, HasId}
import sangria.schema._

object BigRowSchema extends GraphQLSchemaHelper("bigRow") {
  implicit val bigRowPrimaryKeyId: HasId[BigRow, Long] = HasId[BigRow, Long](_.id)
  private[this] def getByPrimaryKeySeq(c: GraphQLContext, idSeq: Seq[Long]) = c.getInstance[services.size.BigRowService].getByPrimaryKeySeq(c.creds, idSeq)(c.trace)
  val bigRowByPrimaryKeyFetcher = Fetcher(getByPrimaryKeySeq)

  val bigRowIdArg = Argument("id", LongType)
  val bigRowIdSeqArg = Argument("ids", ListInputType(LongType))

  val bigRowTArg = Argument("t", StringType)
  val bigRowTSeqArg = Argument("ts", ListInputType(StringType))

  implicit lazy val bigRowType: sangria.schema.ObjectType[GraphQLContext, BigRow] = deriveObjectType(
    sangria.macros.derive.AddFields(
      Field(
        name = "smalls",
        fieldType = ListType(SmallRowSchema.smallRowType),
        resolve = c => SmallRowSchema.smallRowByBigIdFetcher.deferRelSeq(
          SmallRowSchema.smallRowByBigIdRelation, c.value.id
        )
      )
    )
  )

  implicit lazy val bigRowResultType: sangria.schema.ObjectType[GraphQLContext, BigRowResult] = deriveObjectType()

  val queryFields = fields(
    unitField(name = "bigRow", desc = None, t = OptionType(bigRowType), f = (c, td) => {
      c.ctx.getInstance[services.size.BigRowService].getByPrimaryKey(c.ctx.creds, c.arg(bigRowIdArg))(td)
    }, bigRowIdArg),
    unitField(name = "bigRowSeq", desc = None, t = ListType(bigRowType), f = (c, td) => {
      c.ctx.getInstance[services.size.BigRowService].getByPrimaryKeySeq(c.ctx.creds, c.arg(bigRowIdSeqArg))(td)
    }, bigRowIdSeqArg),
    unitField(name = "bigRowSearch", desc = None, t = bigRowResultType, f = (c, td) => {
      runSearch(c.ctx.getInstance[services.size.BigRowService], c, td).map(toResult)
    }, queryArg, reportFiltersArg, orderBysArg, limitArg, offsetArg),
    unitField(name = "bigsByT", desc = None, t = ListType(bigRowType), f = (c, td) => {
      c.ctx.getInstance[services.size.BigRowService].getByT(c.ctx.creds, c.arg(bigRowTArg))(td)
    }, bigRowTArg),
    unitField(name = "bigsByTSeq", desc = None, t = ListType(bigRowType), f = (c, td) => {
      c.ctx.getInstance[services.size.BigRowService].getByTSeq(c.ctx.creds, c.arg(bigRowTSeqArg))(td)
    }, bigRowTSeqArg)
  )

  val bigRowMutationType = ObjectType(
    name = "BigRowMutations",
    fields = fields(
      unitField(name = "create", desc = None, t = OptionType(bigRowType), f = (c, td) => {
        c.ctx.getInstance[services.size.BigRowService].create(c.ctx.creds, c.arg(dataFieldsArg))(td)
      }, dataFieldsArg),
      unitField(name = "update", desc = None, t = OptionType(bigRowType), f = (c, td) => {
        c.ctx.getInstance[services.size.BigRowService].update(c.ctx.creds, c.arg(bigRowIdArg), c.arg(dataFieldsArg))(td).map(_._1)
      }, bigRowIdArg, dataFieldsArg),
      unitField(name = "remove", desc = None, t = bigRowType, f = (c, td) => {
        c.ctx.getInstance[services.size.BigRowService].remove(c.ctx.creds, c.arg(bigRowIdArg))(td)
      }, bigRowIdArg)
    )
  )

  val mutationFields = fields(unitField(name = "bigRow", desc = None, t = bigRowMutationType, f = (_, _) => scala.concurrent.Future.successful(())))

  private[this] def toResult(r: GraphQLSchemaHelper.SearchResult[BigRow]) = {
    BigRowResult(paging = r.paging, filters = r.args.filters, orderBys = r.args.orderBys, totalCount = r.count, results = r.results, durationMs = r.dur)
  }
}
