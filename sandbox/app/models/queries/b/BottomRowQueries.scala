/* Generated File */
package models.queries.b

import com.kyleu.projectile.models.database.{DatabaseField, Row}
import com.kyleu.projectile.models.database.DatabaseFieldType._
import com.kyleu.projectile.models.queries.{BaseQueries, ResultFieldHelper}
import com.kyleu.projectile.models.result.data.DataField
import com.kyleu.projectile.models.result.filter.Filter
import com.kyleu.projectile.models.result.orderBy.OrderBy
import java.util.UUID
import models.b.BottomRow

object BottomRowQueries extends BaseQueries[BottomRow]("bottomRow", "bottom") {
  override val fields = Seq(
    DatabaseField(title = "Id", prop = "id", col = "id", typ = UuidType),
    DatabaseField(title = "Top Id", prop = "topId", col = "top_id", typ = UuidType),
    DatabaseField(title = "T", prop = "t", col = "t", typ = StringType),
    DatabaseField(title = "Dt", prop = "dt", col = "dt", typ = TimestampType)
  )
  override val pkColumns = Seq("id")
  override protected val searchColumns = Seq("id", "top_id", "t")

  def countAll(filters: Seq[Filter] = Nil) = onCountAll(filters)
  def getAll(filters: Seq[Filter] = Nil, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None) = {
    new GetAll(filters, orderBys, limit, offset)
  }

  def search(q: Option[String], filters: Seq[Filter] = Nil, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None) = {
    new Search(q, filters, orderBys, limit, offset)
  }
  def searchCount(q: Option[String], filters: Seq[Filter] = Nil) = new SearchCount(q, filters)
  def searchExact(q: String, orderBys: Seq[OrderBy], limit: Option[Int], offset: Option[Int]) = new SearchExact(q, orderBys, limit, offset)

  def getByPrimaryKey(id: UUID) = new GetByPrimaryKey(Seq(id))
  def getByPrimaryKeySeq(idSeq: Seq[UUID]) = new ColSeqQuery(column = "id", values = idSeq)

  final case class CountById(id: UUID) extends ColCount(column = "id", values = Seq(id))
  final case class GetById(id: UUID, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None) extends SeqQuery(
    whereClause = Some(quote("id") + "  = ?"), orderBy = ResultFieldHelper.orderClause(fields, orderBys: _*),
    limit = limit, offset = offset, values = Seq(id)
  )
  final case class GetByIdSeq(idSeq: Seq[UUID]) extends ColSeqQuery(column = "id", values = idSeq)

  final case class CountByT(t: String) extends ColCount(column = "t", values = Seq(t))
  final case class GetByT(t: String, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None) extends SeqQuery(
    whereClause = Some(quote("t") + "  = ?"), orderBy = ResultFieldHelper.orderClause(fields, orderBys: _*),
    limit = limit, offset = offset, values = Seq(t)
  )
  final case class GetByTSeq(tSeq: Seq[String]) extends ColSeqQuery(column = "t", values = tSeq)

  final case class CountByTopId(topId: UUID) extends ColCount(column = "top_id", values = Seq(topId))
  final case class GetByTopId(topId: UUID, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None) extends SeqQuery(
    whereClause = Some(quote("top_id") + "  = ?"), orderBy = ResultFieldHelper.orderClause(fields, orderBys: _*),
    limit = limit, offset = offset, values = Seq(topId)
  )
  final case class GetByTopIdSeq(topIdSeq: Seq[UUID]) extends ColSeqQuery(column = "top_id", values = topIdSeq)

  def insert(model: BottomRow) = new Insert(model)
  def insertBatch(models: Seq[BottomRow]) = new InsertBatch(models)
  def create(dataFields: Seq[DataField]) = new InsertFields(dataFields)

  def removeByPrimaryKey(id: UUID) = new RemoveByPrimaryKey(Seq[Any](id))

  def update(id: UUID, fields: Seq[DataField]) = new UpdateFields(Seq[Any](id), fields)
  def updateBulk(pks: Seq[Seq[Any]], fields: Seq[DataField]) = new UpdateFieldsBulk(pks, fields)

  override def fromRow(row: Row) = BottomRow(
    id = UuidType(row, "id"),
    topId = UuidType(row, "top_id"),
    t = StringType.opt(row, "t"),
    dt = TimestampType.opt(row, "dt")
  )
}
