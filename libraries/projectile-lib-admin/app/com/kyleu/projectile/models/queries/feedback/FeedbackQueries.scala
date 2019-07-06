/* Generated File */
package com.kyleu.projectile.models.queries.feedback

import com.kyleu.projectile.models.database.{DatabaseField, Row}
import com.kyleu.projectile.models.database.DatabaseFieldType._
import com.kyleu.projectile.models.queries.{BaseQueries, ResultFieldHelper}
import com.kyleu.projectile.models.result.data.DataField
import com.kyleu.projectile.models.result.filter.Filter
import com.kyleu.projectile.models.result.orderBy.OrderBy
import java.time.LocalDateTime
import java.util.UUID
import com.kyleu.projectile.models.feedback.Feedback

object FeedbackQueries extends BaseQueries[Feedback]("feedback", "feedback") {
  override val fields = Seq(
    DatabaseField(title = "Id", prop = "id", col = "id", typ = UuidType),
    DatabaseField(title = "Text", prop = "text", col = "text", typ = StringType),
    DatabaseField(title = "Author Id", prop = "authorId", col = "author_id", typ = UuidType),
    DatabaseField(title = "Author Email", prop = "authorEmail", col = "author_email", typ = StringType),
    DatabaseField(title = "Created", prop = "created", col = "created", typ = TimestampType),
    DatabaseField(title = "Status", prop = "status", col = "status", typ = StringType)
  )
  override protected val pkColumns = Seq("id")
  override protected val searchColumns = Seq("id", "text", "author_id", "author_email", "created", "status")

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

  final case class CountByAuthorEmail(authorEmail: String) extends ColCount(column = "author_email", values = Seq(authorEmail))
  final case class GetByAuthorEmail(authorEmail: String, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None) extends SeqQuery(
    whereClause = Some(quote("author_email") + "  = ?"), orderBy = ResultFieldHelper.orderClause(fields, orderBys: _*),
    limit = limit, offset = offset, values = Seq(authorEmail)
  )
  final case class GetByAuthorEmailSeq(authorEmailSeq: Seq[String]) extends ColSeqQuery(column = "author_email", values = authorEmailSeq)

  final case class CountByAuthorId(authorId: UUID) extends ColCount(column = "author_id", values = Seq(authorId))
  final case class GetByAuthorId(authorId: UUID, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None) extends SeqQuery(
    whereClause = Some(quote("author_id") + "  = ?"), orderBy = ResultFieldHelper.orderClause(fields, orderBys: _*),
    limit = limit, offset = offset, values = Seq(authorId)
  )
  final case class GetByAuthorIdSeq(authorIdSeq: Seq[UUID]) extends ColSeqQuery(column = "author_id", values = authorIdSeq)

  final case class CountByCreated(created: LocalDateTime) extends ColCount(column = "created", values = Seq(created))
  final case class GetByCreated(created: LocalDateTime, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None) extends SeqQuery(
    whereClause = Some(quote("created") + "  = ?"), orderBy = ResultFieldHelper.orderClause(fields, orderBys: _*),
    limit = limit, offset = offset, values = Seq(created)
  )
  final case class GetByCreatedSeq(createdSeq: Seq[LocalDateTime]) extends ColSeqQuery(column = "created", values = createdSeq)

  final case class CountById(id: UUID) extends ColCount(column = "id", values = Seq(id))
  final case class GetById(id: UUID, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None) extends SeqQuery(
    whereClause = Some(quote("id") + "  = ?"), orderBy = ResultFieldHelper.orderClause(fields, orderBys: _*),
    limit = limit, offset = offset, values = Seq(id)
  )
  final case class GetByIdSeq(idSeq: Seq[UUID]) extends ColSeqQuery(column = "id", values = idSeq)

  final case class CountByStatus(status: String) extends ColCount(column = "status", values = Seq(status))
  final case class GetByStatus(status: String, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None) extends SeqQuery(
    whereClause = Some(quote("status") + "  = ?"), orderBy = ResultFieldHelper.orderClause(fields, orderBys: _*),
    limit = limit, offset = offset, values = Seq(status)
  )
  final case class GetByStatusSeq(statusSeq: Seq[String]) extends ColSeqQuery(column = "status", values = statusSeq)

  final case class CountByText(text: String) extends ColCount(column = "text", values = Seq(text))
  final case class GetByText(text: String, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None) extends SeqQuery(
    whereClause = Some(quote("text") + "  = ?"), orderBy = ResultFieldHelper.orderClause(fields, orderBys: _*),
    limit = limit, offset = offset, values = Seq(text)
  )
  final case class GetByTextSeq(textSeq: Seq[String]) extends ColSeqQuery(column = "text", values = textSeq)

  def insert(model: Feedback) = new Insert(model)
  def insertBatch(models: Seq[Feedback]) = new InsertBatch(models)
  def create(dataFields: Seq[DataField]) = new InsertFields(dataFields)

  def removeByPrimaryKey(id: UUID) = new RemoveByPrimaryKey(Seq[Any](id))

  def update(id: UUID, fields: Seq[DataField]) = new UpdateFields(Seq[Any](id), fields)

  override def fromRow(row: Row) = Feedback(
    id = UuidType(row, "id"),
    text = StringType(row, "text"),
    authorId = UuidType(row, "author_id"),
    authorEmail = StringType(row, "author_email"),
    created = TimestampType(row, "created"),
    status = StringType(row, "status")
  )
}
