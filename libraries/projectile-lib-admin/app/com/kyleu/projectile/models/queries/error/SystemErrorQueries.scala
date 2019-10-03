/* Generated File */
package com.kyleu.projectile.models.queries.error

import com.kyleu.projectile.models.database.{DatabaseField, Row}
import com.kyleu.projectile.models.database.DatabaseFieldType._
import com.kyleu.projectile.models.queries.{BaseQueries, ResultFieldHelper}
import com.kyleu.projectile.models.result.data.DataField
import com.kyleu.projectile.models.result.filter.Filter
import com.kyleu.projectile.models.result.orderBy.OrderBy
import java.util.UUID

import com.kyleu.projectile.models.error.SystemError

object SystemErrorQueries extends BaseQueries[SystemError]("systemError", "system_error") {
  override val fields = Seq(
    DatabaseField(title = "Id", prop = "id", col = "id", typ = UuidType),
    DatabaseField(title = "Context", prop = "context", col = "context", typ = StringType),
    DatabaseField(title = "User Id", prop = "userId", col = "user_id", typ = UuidType),
    DatabaseField(title = "Cls", prop = "cls", col = "cls", typ = StringType),
    DatabaseField(title = "Message", prop = "message", col = "message", typ = StringType),
    DatabaseField(title = "Stacktrace", prop = "stacktrace", col = "stacktrace", typ = StringType),
    DatabaseField(title = "Occurred", prop = "occurred", col = "occurred", typ = TimestampType)
  )
  override val pkColumns = Seq("id")
  override protected val searchColumns = Seq("id", "context", "user_id", "cls", "message")

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

  final case class CountByCls(cls: String) extends ColCount(column = "cls", values = Seq(cls))
  final case class GetByCls(cls: String, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None) extends SeqQuery(
    whereClause = Some(quote("cls") + "  = ?"), orderBy = ResultFieldHelper.orderClause(fields, orderBys: _*),
    limit = limit, offset = offset, values = Seq(cls)
  )
  final case class GetByClsSeq(clsSeq: Seq[String]) extends ColSeqQuery(column = "cls", values = clsSeq)

  final case class CountByContext(context: String) extends ColCount(column = "context", values = Seq(context))
  final case class GetByContext(context: String, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None) extends SeqQuery(
    whereClause = Some(quote("context") + "  = ?"), orderBy = ResultFieldHelper.orderClause(fields, orderBys: _*),
    limit = limit, offset = offset, values = Seq(context)
  )
  final case class GetByContextSeq(contextSeq: Seq[String]) extends ColSeqQuery(column = "context", values = contextSeq)

  final case class CountById(id: UUID) extends ColCount(column = "id", values = Seq(id))
  final case class GetById(id: UUID, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None) extends SeqQuery(
    whereClause = Some(quote("id") + "  = ?"), orderBy = ResultFieldHelper.orderClause(fields, orderBys: _*),
    limit = limit, offset = offset, values = Seq(id)
  )
  final case class GetByIdSeq(idSeq: Seq[UUID]) extends ColSeqQuery(column = "id", values = idSeq)

  final case class CountByMessage(message: String) extends ColCount(column = "message", values = Seq(message))
  final case class GetByMessage(message: String, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None) extends SeqQuery(
    whereClause = Some(quote("message") + "  = ?"), orderBy = ResultFieldHelper.orderClause(fields, orderBys: _*),
    limit = limit, offset = offset, values = Seq(message)
  )
  final case class GetByMessageSeq(messageSeq: Seq[String]) extends ColSeqQuery(column = "message", values = messageSeq)

  final case class CountByUserId(userId: UUID) extends ColCount(column = "user_id", values = Seq(userId))
  final case class GetByUserId(userId: UUID, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None) extends SeqQuery(
    whereClause = Some(quote("user_id") + "  = ?"), orderBy = ResultFieldHelper.orderClause(fields, orderBys: _*),
    limit = limit, offset = offset, values = Seq(userId)
  )
  final case class GetByUserIdSeq(userIdSeq: Seq[UUID]) extends ColSeqQuery(column = "user_id", values = userIdSeq)

  def insert(model: SystemError) = new Insert(model)
  def insertBatch(models: Seq[SystemError]) = new InsertBatch(models)
  def create(dataFields: Seq[DataField]) = new InsertFields(dataFields)

  def removeByPrimaryKey(id: UUID) = new RemoveByPrimaryKey(Seq[Any](id))

  def update(id: UUID, fields: Seq[DataField]) = new UpdateFields(Seq[Any](id), fields)
  def updateBulk(pks: Seq[Seq[Any]], fields: Seq[DataField]) = new UpdateFieldsBulk(pks, fields)

  override def fromRow(row: Row) = SystemError(
    id = UuidType(row, "id"),
    context = StringType(row, "context"),
    userId = UuidType.opt(row, "user_id"),
    cls = StringType(row, "cls"),
    message = StringType(row, "message"),
    stacktrace = StringType.opt(row, "stacktrace"),
    occurred = TimestampType(row, "occurred")
  )
}
