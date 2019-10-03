/* Generated File */
package models.queries.size

import com.kyleu.projectile.models.database.{DatabaseField, Row}
import com.kyleu.projectile.models.database.DatabaseFieldType._
import com.kyleu.projectile.models.queries.{BaseQueries, ResultFieldHelper}
import com.kyleu.projectile.models.result.data.DataField
import com.kyleu.projectile.models.result.filter.Filter
import com.kyleu.projectile.models.result.orderBy.OrderBy
import models.size.SmallRow

object SmallRowQueries extends BaseQueries[SmallRow]("smallRow", "small") {
  override val fields = Seq(
    DatabaseField(title = "Id", prop = "id", col = "id", typ = LongType),
    DatabaseField(title = "Big Id", prop = "bigId", col = "big_id", typ = LongType),
    DatabaseField(title = "T", prop = "t", col = "t", typ = StringType)
  )
  override val pkColumns = Seq("id")
  override protected val searchColumns = Seq("id", "big_id", "t")

  def countAll(filters: Seq[Filter] = Nil) = onCountAll(filters)
  def getAll(filters: Seq[Filter] = Nil, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None) = {
    new GetAll(filters, orderBys, limit, offset)
  }

  def search(q: Option[String], filters: Seq[Filter] = Nil, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None) = {
    new Search(q, filters, orderBys, limit, offset)
  }
  def searchCount(q: Option[String], filters: Seq[Filter] = Nil) = new SearchCount(q, filters)
  def searchExact(q: String, orderBys: Seq[OrderBy], limit: Option[Int], offset: Option[Int]) = new SearchExact(q, orderBys, limit, offset)

  def getByPrimaryKey(id: Long) = new GetByPrimaryKey(Seq(id))
  def getByPrimaryKeySeq(idSeq: Seq[Long]) = new ColSeqQuery(column = "id", values = idSeq)

  final case class CountByBigId(bigId: Long) extends ColCount(column = "big_id", values = Seq(bigId))
  final case class GetByBigId(bigId: Long, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None) extends SeqQuery(
    whereClause = Some(quote("big_id") + "  = ?"), orderBy = ResultFieldHelper.orderClause(fields, orderBys: _*),
    limit = limit, offset = offset, values = Seq(bigId)
  )
  final case class GetByBigIdSeq(bigIdSeq: Seq[Long]) extends ColSeqQuery(column = "big_id", values = bigIdSeq)

  final case class CountById(id: Long) extends ColCount(column = "id", values = Seq(id))
  final case class GetById(id: Long, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None) extends SeqQuery(
    whereClause = Some(quote("id") + "  = ?"), orderBy = ResultFieldHelper.orderClause(fields, orderBys: _*),
    limit = limit, offset = offset, values = Seq(id)
  )
  final case class GetByIdSeq(idSeq: Seq[Long]) extends ColSeqQuery(column = "id", values = idSeq)

  final case class CountByT(t: String) extends ColCount(column = "t", values = Seq(t))
  final case class GetByT(t: String, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None) extends SeqQuery(
    whereClause = Some(quote("t") + "  = ?"), orderBy = ResultFieldHelper.orderClause(fields, orderBys: _*),
    limit = limit, offset = offset, values = Seq(t)
  )
  final case class GetByTSeq(tSeq: Seq[String]) extends ColSeqQuery(column = "t", values = tSeq)

  def insert(model: SmallRow) = new InsertNoPk(model)
  def insertBatch(models: Seq[SmallRow]) = new InsertBatch(models)
  def create(dataFields: Seq[DataField]) = new InsertFieldsNoPk(dataFields)

  def removeByPrimaryKey(id: Long) = new RemoveByPrimaryKey(Seq[Any](id))

  def update(id: Long, fields: Seq[DataField]) = new UpdateFields(Seq[Any](id), fields)
  def updateBulk(pks: Seq[Seq[Any]], fields: Seq[DataField]) = new UpdateFieldsBulk(pks, fields)

  override def fromRow(row: Row) = SmallRow(
    id = LongType(row, "id"),
    bigId = LongType(row, "big_id"),
    t = StringType.opt(row, "t")
  )
}
