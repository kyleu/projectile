package com.kyleu.projectile.models.queries.permission

import java.time.LocalDateTime
import java.util.UUID

import com.kyleu.projectile.models.auth.Permission
import com.kyleu.projectile.models.database.DatabaseFieldType.{BooleanType, StringType, TimestampType, UuidType}
import com.kyleu.projectile.models.database.{DatabaseField, Row}
import com.kyleu.projectile.models.queries.{BaseQueries, ResultFieldHelper}
import com.kyleu.projectile.models.result.data.DataField
import com.kyleu.projectile.models.result.filter.Filter
import com.kyleu.projectile.models.result.orderBy.OrderBy

object PermissionQueries extends BaseQueries[Permission]("Permission", "system_permission") {
  override val fields = Seq(
    DatabaseField(title = "Role", prop = "role", col = "role", typ = StringType),
    DatabaseField(title = "Package", prop = "pkg", col = "pkg", typ = StringType),
    DatabaseField(title = "Model", prop = "model", col = "model", typ = StringType),
    DatabaseField(title = "Action", prop = "action", col = "action", typ = StringType),
    DatabaseField(title = "Allow", prop = "allow", col = "allow", typ = BooleanType),
    DatabaseField(title = "Created", prop = "created", col = "created", typ = TimestampType),
    DatabaseField(title = "Created By", prop = "createdBy", col = "created_by", typ = UuidType)
  )
  override val pkColumns = Seq("role", "pkg", "model", "action")
  override protected val searchColumns = Seq("role", "pkg", "model", "action", "created_by")

  override protected def toDataSeq(t: Permission) = {
    Seq(t.role, t.pkg.getOrElse(""), t.model.getOrElse(""), t.action.getOrElse(""), t.allow, t.created, t.createdBy)
  }

  def countAll(filters: Seq[Filter] = Nil) = onCountAll(filters)
  def getAll(filters: Seq[Filter] = Nil, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None) = {
    new GetAll(filters, orderBys, limit, offset)
  }

  def search(q: Option[String], filters: Seq[Filter] = Nil, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None) = {
    new Search(q, filters, orderBys, limit, offset)
  }
  def searchCount(q: Option[String], filters: Seq[Filter] = Nil) = new SearchCount(q, filters)
  def searchExact(q: String, orderBys: Seq[OrderBy], limit: Option[Int], offset: Option[Int]) = new SearchExact(q, orderBys, limit, offset)

  def getByPrimaryKey(role: String, pkg: String, model: String, action: String) = new GetByPrimaryKey(Seq(role, pkg, model, action))

  final case class CountByRole(role: String) extends ColCount(column = "role", values = Seq(role))
  final case class GetByRole(role: String, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None) extends SeqQuery(
    whereClause = Some(quote("role") + "  = ?"), orderBy = ResultFieldHelper.orderClause(fields, orderBys: _*),
    limit = limit, offset = offset, values = Seq(role)
  )
  final case class GetByRoleSeq(roleSeq: Seq[String]) extends ColSeqQuery(column = "role", values = roleSeq)

  final case class CountByCreated(created: LocalDateTime) extends ColCount(column = "created", values = Seq(created))
  final case class GetByCreated(created: LocalDateTime, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None) extends SeqQuery(
    whereClause = Some(quote("created") + "  = ?"), orderBy = ResultFieldHelper.orderClause(fields, orderBys: _*),
    limit = limit, offset = offset, values = Seq(created)
  )
  final case class GetByCreatedSeq(createdSeq: Seq[LocalDateTime]) extends ColSeqQuery(column = "created", values = createdSeq)

  final case class CountByCreatedBy(createdBy: UUID) extends ColCount(column = "created_by", values = Seq(createdBy))
  final case class GetByCreatedBy(createdBy: UUID, orderBys: Seq[OrderBy] = Nil, limit: Option[Int] = None, offset: Option[Int] = None) extends SeqQuery(
    whereClause = Some(quote("created_by") + "  = ?"), orderBy = ResultFieldHelper.orderClause(fields, orderBys: _*),
    limit = limit, offset = offset, values = Seq(createdBy)
  )
  final case class GetByCreatedBySeq(createdBySeq: Seq[UUID]) extends ColSeqQuery(column = "created_by", values = createdBySeq)

  def insert(model: Permission) = new Insert(model)
  def insertBatch(models: Seq[Permission]) = new InsertBatch(models)
  def create(dataFields: Seq[DataField]) = new InsertFields(dataFields)

  def removeByPrimaryKey(role: String, pkg: String, model: String, action: String) = new RemoveByPrimaryKey(Seq[Any](role, pkg, model, action))

  def update(role: String, pkg: String, model: String, action: String, fields: Seq[DataField]) = new UpdateFields(Seq[Any](role, pkg, model, action), fields)

  override def fromRow(row: Row) = Permission(
    role = StringType(row, "role"),
    pkg = Some(StringType(row, "pkg")).filter(_.nonEmpty),
    model = Some(StringType(row, "model")).filter(_.nonEmpty),
    action = Some(StringType(row, "action")).filter(_.nonEmpty),
    allow = BooleanType(row, "allow"),
    created = TimestampType(row, "created"),
    createdBy = UuidType.opt(row, "created_by")
  )
}
