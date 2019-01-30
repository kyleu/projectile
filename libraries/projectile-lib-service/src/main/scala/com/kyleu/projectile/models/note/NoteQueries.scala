package com.kyleu.projectile.models.note

import com.kyleu.projectile.models.database.DatabaseFieldType.{StringType, TimestampType, UuidType}
import com.kyleu.projectile.models.database.{Query, Row}

object NoteQueries {
  private[this] val tableName = "note"

  final case class GetByModel(model: String, pk: String) extends Query[Seq[Note]] {
    override val name = "get.notes.by.model"
    override val sql = s"""select * from "$tableName" where "rel_type" = ? and "rel_pk" = ?"""
    override val values = Seq(model, pk)
    override def reduce(rows: Iterator[Row]) = rows.map(fromRow).toList
  }

  final case class GetByModelSeq(models: Seq[(String, String)]) extends Query[Seq[Note]] {
    override val name = "get.notes.by.model.seq"
    val clause = """("rel_type" = ? and "rel_pk" = ?)"""
    override val sql = s"""select * from "$tableName" where ${models.map(_ => clause).mkString(" or ")}"""
    override val values = models.flatMap(x => Seq(x._1, x._2))
    override def reduce(rows: Iterator[Row]) = rows.map(fromRow).toList
  }

  private[this] def fromRow(row: Row) = Note(
    id = UuidType(row, "id"),
    relType = StringType.opt(row, "rel_type"),
    relPk = StringType.opt(row, "rel_pk"),
    text = StringType(row, "text"),
    author = UuidType(row, "author"),
    created = TimestampType(row, "created")
  )
}
