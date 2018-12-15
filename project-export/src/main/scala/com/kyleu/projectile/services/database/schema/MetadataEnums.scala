package com.kyleu.projectile.services.database.schema

import java.sql.Connection

import com.kyleu.projectile.models.database.query.Query
import com.kyleu.projectile.models.database.schema.EnumType
import com.kyleu.projectile.services.database.query.{JdbcRow, QueryExecutor}
import com.kyleu.projectile.util.Logging

object MetadataEnums extends Logging {
  case object EnumQuery extends Query[Seq[EnumType]] {
    override def sql = """
      select t.typname, e.enumlabel
      from pg_enum e
      join pg_type t on e.enumtypid = t.oid
      where t.typname != 'myenum'
      order by t.typname, e.enumsortorder
    """
    override def reduce(rows: Iterator[JdbcRow]) = rows.map { row =>
      (row.as[String]("typname"), row.as[String]("enumlabel"))
    }.toSeq.groupBy(_._1).map(e => EnumType(e._1, e._2.map(_._2))).toSeq
  }

  def getEnums(conn: Connection) = new QueryExecutor(conn)(EnumQuery)
}
