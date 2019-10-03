package com.kyleu.projectile.models.queries

import com.kyleu.projectile.models.database.{FlatSingleRowQuery, Row, Statement}
import com.kyleu.projectile.models.result.data.DataField

trait MutationQueries[T <: Product] { this: BaseQueries[T] =>
  protected def pkWhereClause = pkColumns.map(x => quote(x) + " = ?").mkString(" and ")

  protected class Insert(model: T) extends Statement {
    override val name = s"$key.insert"
    override val sql = insertSql
    override val values: Seq[Any] = toDataSeq(model)
  }

  protected class InsertNoPk(model: T) extends FlatSingleRowQuery[Seq[Any]] {
    override val name = s"$key.insert.nopk"
    override val sql = insertSqlNoPk
    override val values: Seq[Any] = toDataSeqNoPk(model)
    override def flatMap(row: Row) = Some(row.toSeq.flatten)
  }

  protected class InsertBatch(models: Seq[T]) extends Statement {
    override val name = s"$key.insert.batch"
    private[this] val valuesClause = models.map(_ => s"($columnPlaceholders)").mkString(", ")
    override val sql = s"""insert into ${quote(tableName)} ($quotedColumns) values $valuesClause"""
    override val values: Seq[Any] = models.flatMap(toDataSeq)
  }

  protected class RemoveByPrimaryKey(override val values: Seq[Any]) extends Statement {
    override val name = s"$key.remove.by.primary.key"
    override val sql = s"""delete from ${quote(tableName)} where $pkWhereClause"""
  }

  protected class InsertFields(dataFields: Seq[DataField]) extends Statement {
    override val name = s"$key.insert.fields"
    private[this] val cols = dataFields.map(f => ResultFieldHelper.sqlForField("insert", f.k, fields))
    override val sql = s"""insert into ${quote(tableName)} (${cols.mkString(", ")}) values (${cols.map(_ => "?").mkString(", ")})"""
    override val values = dataFields.map(f => ResultFieldHelper.valueForField("create", f.k, f.v, fields))
  }

  protected class InsertFieldsNoPk(dataFields: Seq[DataField]) extends FlatSingleRowQuery[Seq[Any]] {
    private[this] val noPk = dataFields.filterNot(f => pkColumns.contains(f.k))
    override val name = s"$key.insert.fields"
    private[this] val cols = noPk.map(f => ResultFieldHelper.sqlForField("insert", f.k, fields))
    override val sql = s"""insert into ${quote(tableName)} (${cols.mkString(", ")}) values (${cols.map(_ => "?").mkString(", ")}) $returnClause"""
    override val values = noPk.map(f => ResultFieldHelper.valueForField("create", f.k, f.v, fields))
    override def flatMap(row: Row) = Some(row.toSeq.flatten)
  }

  protected class UpdateFields(pks: Seq[Any], dataFields: Seq[DataField]) extends Statement {
    override val name = s"$key.update.fields"
    private[this] val cols = dataFields.map(f => ResultFieldHelper.sqlForField("update", f.k, fields))
    override val sql = s"""update ${quote(tableName)} set ${cols.map(_ + " = ?").mkString(", ")} where $pkWhereClause"""
    override val values = dataFields.map(f => ResultFieldHelper.valueForField("update", f.k, f.v, fields)) ++ pks
  }

  protected class UpdateFieldsBulk(pks: Seq[Seq[Any]], dataFields: Seq[DataField]) extends Statement {
    override val name = s"$key.update.fields"
    private[this] val cols = dataFields.map(f => ResultFieldHelper.sqlForField("update", f.k, fields))
    private[this] val whereClause = pks.map(_ => "(" + pkWhereClause + ")").mkString(" or ")
    override val sql = s"""update ${quote(tableName)} set ${cols.map(_ + " = ?").mkString(", ")} where $whereClause"""
    override val values = dataFields.map(f => ResultFieldHelper.valueForField("update", f.k, f.v, fields)) ++ pks.flatten
  }
}
