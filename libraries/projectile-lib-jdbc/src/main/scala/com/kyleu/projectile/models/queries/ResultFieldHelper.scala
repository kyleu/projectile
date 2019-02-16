package com.kyleu.projectile.models.queries

import com.kyleu.projectile.models.database.DatabaseField
import com.kyleu.projectile.models.database.DatabaseFieldType._
import com.kyleu.projectile.models.result.filter.{Filter, FilterOp}
import com.kyleu.projectile.models.result.orderBy.OrderBy
import com.kyleu.projectile.util.{DateUtils, EncryptionUtils, StringUtils}

object ResultFieldHelper {
  def sqlForField(t: String, field: String, fields: Seq[DatabaseField]) = fields.find(_.prop == field) match {
    case Some(f) => EngineHelper.quote(f.col)
    case None => throw new IllegalStateException(s"Invalid $t field [$field]. Allowed fields are [${fields.map(_.prop).mkString(", ")}]")
  }

  def valueForField(t: String, field: String, value: Option[String], fields: Seq[DatabaseField]): Option[Any] = fields.find(_.prop == field) match {
    case Some(f) =>
      def v = value.map(_.trim).filter(_.nonEmpty)
      f.typ match {
        case EncryptedStringType => v.map(EncryptionUtils.encrypt)
        case DateType => v.map(DateUtils.fromDateString)
        case TimeType => v.map(DateUtils.fromTimeString)
        case TimestampType => v.map(DateUtils.sqlDateTimeFromString)
        case TimestampZonedType => v.map(DateUtils.sqlDateTimeFromString)
        case TagsType => v.map(s => StringUtils.toMap(s).map(x => "\"" + x._1 + "\" => \"" + x._2 + "\"").mkString(","))
        case _ if f.typ.isList => v.map(s => "{" + StringUtils.toList(s).map("\"" + _ + "\"").mkString(",") + "}")
        case _ => value
      }
    case None => throw new IllegalStateException(s"Invalid $t field [$field]. Allowed fields are [${fields.map(_.prop).mkString(", ")}]")
  }

  def orderClause(fields: Seq[DatabaseField], orderBys: OrderBy*) = if (orderBys.isEmpty) {
    None
  } else {
    Some(orderBys.map(orderBy => sqlForField("order by", orderBy.col, fields) + " " + orderBy.dir.sql).mkString(", "))
  }

  def filterClause(filters: Seq[Filter], fields: Seq[DatabaseField], add: Option[String] = None) = if (filters.isEmpty) {
    add
  } else {
    val clauses = filters.map { filter =>
      val col = sqlForField("where clause", filter.k, fields)
      val vals = filter.v.map(_ => "?").mkString(", ")
      filter.o match {
        case FilterOp.Equal => s"$col in ($vals)"
        case FilterOp.NotEqual => s"$col not in ($vals)"
        case FilterOp.Like => "(" + filter.v.map(_ => s"$col like ?").mkString(" or ") + ")"
        case FilterOp.GreaterThanOrEqual => "(" + vals.map(_ => s"$col >= ?").mkString(" or ") + ")"
        case FilterOp.LessThanOrEqual => "(" + vals.map(_ => s"$col <= ?").mkString(" or ") + ")"
        case x => throw new IllegalStateException(s"Operation [$x] is not currently supported")
      }
    }
    val clauseString = clauses.mkString(" and ")
    add.map(f => s"($clauseString) and $f").orElse(Some(clauseString))
  }

  def getResultSql(filters: Seq[Filter], orderBys: Seq[OrderBy] = Nil, fields: Seq[DatabaseField] = Nil, add: Option[String] = None) = {
    filterClause(filters, fields, add) -> orderClause(fields, orderBys: _*)
  }
}
