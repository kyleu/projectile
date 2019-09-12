/* Generated File */
package models.graphql

import com.kyleu.projectile.graphql.{GraphQLContext, GraphQLSchemaHelper}
import com.kyleu.projectile.graphql.GraphQLUtils._
import java.util.UUID
import models.{TopRow, TopRowResult}
import sangria.execution.deferred.{Fetcher, HasId}
import sangria.schema._

object TopRowSchema extends GraphQLSchemaHelper("topRow") {
  implicit val topRowPrimaryKeyId: HasId[TopRow, UUID] = HasId[TopRow, UUID](_.id)
  private[this] def getByPrimaryKeySeq(c: GraphQLContext, idSeq: Seq[UUID]) = c.getInstance[services.TopRowService].getByPrimaryKeySeq(c.creds, idSeq)(c.trace)
  val topRowByPrimaryKeyFetcher = Fetcher(getByPrimaryKeySeq)

  val topRowIdArg = Argument("id", uuidType)
  val topRowIdSeqArg = Argument("ids", ListInputType(uuidType))

  val topRowTArg = Argument("t", StringType)
  val topRowTSeqArg = Argument("ts", ListInputType(StringType))

  implicit lazy val topRowType: sangria.schema.ObjectType[GraphQLContext, TopRow] = deriveObjectType(
    sangria.macros.derive.AddFields(
      Field(
        name = "bottomTopIdFkey",
        fieldType = ListType(BottomRowSchema.bottomRowType),
        resolve = c => BottomRowSchema.bottomRowByTopIdFetcher.deferRelSeq(
          BottomRowSchema.bottomRowByTopIdRelation, c.value.id
        )
      )
    )
  )

  implicit lazy val topRowResultType: sangria.schema.ObjectType[GraphQLContext, TopRowResult] = deriveObjectType()

  val queryFields = fields(
    unitField(name = "topRow", desc = None, t = OptionType(topRowType), f = (c, td) => {
      c.ctx.getInstance[services.TopRowService].getByPrimaryKey(c.ctx.creds, c.arg(topRowIdArg))(td)
    }, topRowIdArg),
    unitField(name = "topRowSeq", desc = None, t = ListType(topRowType), f = (c, td) => {
      c.ctx.getInstance[services.TopRowService].getByPrimaryKeySeq(c.ctx.creds, c.arg(topRowIdSeqArg))(td)
    }, topRowIdSeqArg),
    unitField(name = "topRowSearch", desc = None, t = topRowResultType, f = (c, td) => {
      runSearch(c.ctx.getInstance[services.TopRowService], c, td).map(toResult)
    }, queryArg, reportFiltersArg, orderBysArg, limitArg, offsetArg),
    unitField(name = "topsByT", desc = None, t = ListType(topRowType), f = (c, td) => {
      c.ctx.getInstance[services.TopRowService].getByT(c.ctx.creds, c.arg(topRowTArg))(td)
    }, topRowTArg),
    unitField(name = "topsByTSeq", desc = None, t = ListType(topRowType), f = (c, td) => {
      c.ctx.getInstance[services.TopRowService].getByTSeq(c.ctx.creds, c.arg(topRowTSeqArg))(td)
    }, topRowTSeqArg)
  )

  val topRowMutationType = ObjectType(
    name = "TopRowMutations",
    fields = fields(
      unitField(name = "create", desc = None, t = OptionType(topRowType), f = (c, td) => {
        c.ctx.getInstance[services.TopRowService].create(c.ctx.creds, c.arg(dataFieldsArg))(td)
      }, dataFieldsArg),
      unitField(name = "update", desc = None, t = OptionType(topRowType), f = (c, td) => {
        c.ctx.getInstance[services.TopRowService].update(c.ctx.creds, c.arg(topRowIdArg), c.arg(dataFieldsArg))(td).map(_._1)
      }, topRowIdArg, dataFieldsArg),
      unitField(name = "remove", desc = None, t = topRowType, f = (c, td) => {
        c.ctx.getInstance[services.TopRowService].remove(c.ctx.creds, c.arg(topRowIdArg))(td)
      }, topRowIdArg)
    )
  )

  val mutationFields = fields(unitField(name = "topRow", desc = None, t = topRowMutationType, f = (_, _) => scala.concurrent.Future.successful(())))

  private[this] def toResult(r: GraphQLSchemaHelper.SearchResult[TopRow]) = {
    TopRowResult(paging = r.paging, filters = r.args.filters, orderBys = r.args.orderBys, totalCount = r.count, results = r.results, durationMs = r.dur)
  }
}
